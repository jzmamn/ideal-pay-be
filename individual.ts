import {
  ChangeDetectionStrategy, Component, DestroyRef,
  computed, inject, signal, OnInit,
} from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { IndividualSalaryService } from './individual-salary/shared/individual-salary.service';
import { TableAutocomplete, type TableColumn } from '../../../shared/components/table-autocomplete/table-autocomplete';
import { type EmployeeResponse } from '../../settings/employee/employee.model';

type WorkflowStep = 'prepare' | 'review' | 'approve' | 'disburse';

// ── Sub-step config (mirrors batch SECTION_CONFIG) ────────────────────────────

export const SUB_STEPS = [
  { uiKey: 'fixedAlw',  label: 'Fixed Allowance'    },
  { uiKey: 'varAlw',    label: 'Variable Allowance'  },
  { uiKey: 'overtime',  label: 'Overtime'            },
  { uiKey: 'fixedDed',  label: 'Fixed Deduction'     },
  { uiKey: 'varDed',    label: 'Variable Deduction'  },
  { uiKey: 'nopay',     label: 'NoPay'               },
  { uiKey: 'loans',     label: 'Loans'               },
  { uiKey: 'bonus',     label: 'Bonus'               },
] as const;

// ── Payroll profile response types ────────────────────────────────────────────

export interface EmployeeFixedAllowanceItem {
  id?: number;
  isAssigned: boolean;
  faId: number;
  faCode: string;
  faName: string;
  amount?: number;
  payrollMonth?: string;
  isProcessed?: boolean;
}

export interface EmployeeFixedDeductionItem {
  id?: number;
  isAssigned: boolean;
  fdId: number;
  fdCode: string;
  fdName: string;
  amount?: number;
  payrollMonth?: string;
  isProcessed?: boolean;
}

export interface EmployeeVariableAllowanceItem {
  id?: number;
  isAssigned: boolean;
  vaId: number;
  vaCode: string;
  vaName: string;
  amount?: number;
  payrollMonth?: string;
}

export interface EmployeeVariableDeductionItem {
  id?: number;
  isAssigned: boolean;
  vdId: number;
  vdCode: string;
  vdName: string;
  amount?: number;
  payrollMonth?: string;
}

export interface EmployeeNopayItem {
  id?: number;
  isAssigned: boolean;
  nopayId: number;
  nopayCode: string;
  nopayName: string;
  days?: number;
  amount?: number;
  payrollMonth?: string;
}

export interface EmployeeOvertimeItem {
  id?: number;
  isAssigned: boolean;
  overtimeId: number;
  overtimeCode: string;
  overtimeName: string;
  hours?: number;
  amount?: number;
  payrollMonth?: string;
}

export interface EmployeePayrollProfile {
  employee: EmployeeResponse;
  fixedAllowances: EmployeeFixedAllowanceItem[];
  fixedDeductions: EmployeeFixedDeductionItem[];
  variableAllowances: EmployeeVariableAllowanceItem[];
  variableDeductions: EmployeeVariableDeductionItem[];
  nopays: EmployeeNopayItem[];
  overtimes: EmployeeOvertimeItem[];
}

interface ApiResponse<T> {
  data: T;
  message?: string;
}

// ── Sri Lanka PAYE monthly tax brackets (annual thresholds ÷ 12) ─────────────

function calcIncomeTax(taxableIncome: number): number {
  if (taxableIncome <= 0) return 0;
  const brackets: [number, number][] = [
    [100_000, 0.00], [41_667, 0.06], [41_667, 0.12], [41_667, 0.18],
    [41_667, 0.24],  [41_667, 0.30], [Infinity, 0.36],
  ];
  let tax = 0;
  let remaining = taxableIncome;
  for (const [limit, rate] of brackets) {
    if (remaining <= 0) break;
    tax += Math.min(remaining, limit) * rate;
    remaining -= limit;
  }
  return tax;
}

@Component({
  selector: 'app-individual',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DecimalPipe, DatePipe, ReactiveFormsModule,
    MatButtonModule, MatCheckboxModule, MatDividerModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatProgressSpinnerModule,
    MatSelectModule, MatTabsModule,
    TableAutocomplete,
  ],
  templateUrl: './individual.html',
  styleUrl: './individual.scss',
})
export class IndividualComponent implements OnInit {
  readonly svc             = inject(IndividualSalaryService);
  private readonly http        = inject(HttpClient);
  private readonly destroyRef  = inject(DestroyRef);
  private readonly fb          = inject(FormBuilder);

  readonly SUB_STEPS       = SUB_STEPS;

  readonly employeeCtrl     = new FormControl<number | null>(null);
  readonly selectedEmployee = signal<EmployeeResponse | null>(null);
  readonly employeeProfile  = signal<EmployeePayrollProfile | null>(null);
  readonly profileLoading   = signal(false);
  readonly selectedSubStep  = signal(0);
  readonly workflowStep     = signal<WorkflowStep>('prepare');
  readonly saving           = signal(false);
  readonly submitting       = signal(false);
  readonly isDisbursed      = signal(false);
  readonly lastSavedAt      = signal<Date | null>(null);

  readonly lastSavedLabel = computed(() => {
    const d = this.lastSavedAt();
    if (!d) return '';
    const hh = d.getHours().toString().padStart(2, '0');
    const mm = d.getMinutes().toString().padStart(2, '0');
    return `Saved at ${hh}:${mm}`;
  });

  readonly approveForm = this.fb.group({
    approvedBy: this.fb.nonNullable.control('', Validators.required),
    remarks:    this.fb.nonNullable.control(''),
  });

  readonly employeeCols: TableColumn<EmployeeResponse>[] = [
    { key: 'employeeNo', label: 'Emp #' },
    { key: 'firstName',  label: 'First Name' },
    { key: 'lastName',   label: 'Last Name' },
  ];

  readonly empDisplayFn = (item: EmployeeResponse): string =>
    `${item.firstName} ${item.lastName} — ${item.employeeNo}`;

  // ── Derived from profile ───────────────────────────────────────────────────

  readonly assignedFixedAllowances = computed(() =>
    this.employeeProfile()?.fixedAllowances.filter(x => x.isAssigned) ?? []);

  readonly assignedFixedDeductions = computed(() =>
    this.employeeProfile()?.fixedDeductions.filter(x => x.isAssigned) ?? []);

  // ── Salary computations ────────────────────────────────────────────────────

  readonly basicSalary = computed(() => this.selectedEmployee()?.basicSalary ?? 0);

  private readonly selectedEntry = computed(() => {
    const emp = this.selectedEmployee();
    if (!emp) return null;
    return this.svc.entries().find(e => e.employeeId === emp.id) ?? null;
  });

  readonly grossPay         = computed(() => this.selectedEntry()?.grossPay ?? this.basicSalary());
  readonly epfEmployee      = computed(() => Math.round(this.basicSalary() * 0.11 * 100) / 100);
  readonly taxableIncome    = computed(() => Math.max(0, this.grossPay() - this.epfEmployee()));
  readonly incomeTax        = computed(() => Math.round(calcIncomeTax(this.taxableIncome()) * 100) / 100);
  readonly totalDeductions  = computed(() =>
    this.selectedEntry()?.totalDeductions ?? (this.epfEmployee() + this.incomeTax()));
  readonly netPay           = computed(() =>
    this.selectedEntry()?.netPay ?? (this.grossPay() - this.totalDeductions()));
  readonly epfEmployer      = computed(() => Math.round(this.basicSalary() * 0.13 * 100) / 100);
  readonly etfEmployer      = computed(() => Math.round(this.basicSalary() * 0.03 * 100) / 100);
  readonly totalEmployerCost = computed(() => this.grossPay() + this.epfEmployer() + this.etfEmployer());

  readonly periodLabel = this.svc.periodLabel;

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.svc.loadEntries();

    const id = setInterval(async () => {
      if (this.svc.dirtyCount() > 0) {
        try {
          await this.svc.saveAll();
          this.lastSavedAt.set(new Date());
        } catch { /* already toasted by service */ }
      }
    }, 60_000);

    this.destroyRef.onDestroy(() => clearInterval(id));
  }

  // ── Event handlers ─────────────────────────────────────────────────────────

  onEmployeeSelected(item: unknown): void {
    const emp = item as EmployeeResponse;
    this.selectedEmployee.set(emp);
    this.employeeProfile.set(null);
    this.workflowStep.set('prepare');
    this.selectedSubStep.set(0);
    this.loadEmployeeProfile(emp.id);
  }

  // ── Profile fetch ──────────────────────────────────────────────────────────

  private async loadEmployeeProfile(empId: number): Promise<void> {
    this.profileLoading.set(true);
    try {
      const res = await firstValueFrom(
        this.http.get<ApiResponse<EmployeePayrollProfile>>(
          `/payroll/emp-profile/${empId}?assignedOnly=false`
        )
      );
      this.employeeProfile.set(res.data);
    } catch {
      /* handle silently or toast as needed */
    } finally {
      this.profileLoading.set(false);
    }
  }

  // ── Component row update helpers ───────────────────────────────────────────

  updateFixedAllowance(idx: number, changes: Partial<EmployeeFixedAllowanceItem>): void {
    this.employeeProfile.update(p => {
      if (!p) return p;
      const items = [...p.fixedAllowances];
      items[idx] = { ...items[idx], ...changes };
      return { ...p, fixedAllowances: items };
    });
  }

  updateFixedDeduction(idx: number, changes: Partial<EmployeeFixedDeductionItem>): void {
    this.employeeProfile.update(p => {
      if (!p) return p;
      const items = [...p.fixedDeductions];
      items[idx] = { ...items[idx], ...changes };
      return { ...p, fixedDeductions: items };
    });
  }

  updateVariableAllowance(idx: number, changes: Partial<EmployeeVariableAllowanceItem>): void {
    this.employeeProfile.update(p => {
      if (!p) return p;
      const items = [...p.variableAllowances];
      items[idx] = { ...items[idx], ...changes };
      return { ...p, variableAllowances: items };
    });
  }

  updateVariableDeduction(idx: number, changes: Partial<EmployeeVariableDeductionItem>): void {
    this.employeeProfile.update(p => {
      if (!p) return p;
      const items = [...p.variableDeductions];
      items[idx] = { ...items[idx], ...changes };
      return { ...p, variableDeductions: items };
    });
  }

  updateOvertime(idx: number, changes: Partial<EmployeeOvertimeItem>): void {
    this.employeeProfile.update(p => {
      if (!p) return p;
      const items = [...p.overtimes];
      items[idx] = { ...items[idx], ...changes };
      return { ...p, overtimes: items };
    });
  }

  updateNopay(idx: number, changes: Partial<EmployeeNopayItem>): void {
    this.employeeProfile.update(p => {
      if (!p) return p;
      const items = [...p.nopays];
      items[idx] = { ...items[idx], ...changes };
      return { ...p, nopays: items };
    });
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  async saveDraft(): Promise<void> {
    this.saving.set(true);
    try {
      await this.svc.saveAll();
      this.lastSavedAt.set(new Date());
    } finally {
      this.saving.set(false);
    }
  }

  async recalculate(): Promise<void> {
    await this.svc.recalculate();
  }

  async submitForReview(): Promise<void> {
    this.submitting.set(true);
    try {
      if (this.svc.dirtyCount() > 0) await this.saveDraft();
      this.workflowStep.set('review');
    } finally {
      this.submitting.set(false);
    }
  }

  confirmReview(): void {
    this.workflowStep.set('approve');
  }

  approvePayroll(): void {
    this.approveForm.markAllAsTouched();
    if (this.approveForm.invalid) return;
    this.workflowStep.set('disburse');
  }

  disburse(): void {
    this.isDisbursed.set(true);
  }

  resetWorkflow(): void {
    this.workflowStep.set('prepare');
    this.isDisbursed.set(false);
    this.approveForm.reset();
    this.employeeCtrl.reset();
    this.selectedEmployee.set(null);
    this.employeeProfile.set(null);
    this.selectedSubStep.set(0);
  }
}
