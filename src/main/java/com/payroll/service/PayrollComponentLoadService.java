package com.payroll.service;

import com.payroll.dto.response.LoadSummaryDTO;

/**
 * Payroll component load service.
 *
 * <p>For every active payroll component that has a configured formula or amount,
 * this service creates / updates employee transaction records ({@code emp_fa},
 * {@code emp_fd}, {@code emp_ot}, {@code emp_np}, {@code emp_late}, {@code emp_bonus})
 * for all active employees (or a single specified employee).
 *
 * <h3>Rules per component type</h3>
 * <ul>
 *   <li><b>Fixed Allowance / Fixed Deduction</b>: formula evaluated → stored as
 *       {@code emp_fa.amount} / {@code emp_fd.amount}; amount is read-only in UI.</li>
 *   <li><b>Overtime</b>: formula or default ({@code basicSalary / (workingDays * 8)})
 *       → stored as {@code emp_ot.rate}; UI users enter hours; amount = rate × hours.</li>
 *   <li><b>No Pay</b>: formula or default ({@code basicSalary / workingDays})
 *       → stored as {@code emp_np.rate}; UI users enter days; amount = rate × days.</li>
 *   <li><b>Late Deduction</b>: formula or default ({@code basicSalary /
 *       (config.workingDays * config.workingHoursPerDay)}) → stored as
 *       {@code emp_late.rate}; UI users enter hours; amount = rate × hours.</li>
 *   <li><b>Bonus</b>: formula or bonus.amount → stored as {@code emp_bonus.amount};
 *       amount remains <em>editable</em> after load.</li>
 * </ul>
 *
 * <p>Formula evaluation occurs <em>only</em> here. Payroll processing
 * ({@code SalaryCalculationEngineService}) reads the pre-stored values.
 * A subsequent call to any load method refreshes values from the latest
 * component configuration.
 */
public interface PayrollComponentLoadService {

    /**
     * Loads all active payroll components for all active employees in the given period.
     * Working days are resolved from the {@code PayrollPeriod} record for that month/year.
     *
     * @param month  payroll period month (1–12)
     * @param year   payroll period year (e.g. 2026)
     * @param userId ID of the user triggering the load (recorded as createdBy/modifiedBy)
     * @return summary of records processed and any non-fatal errors
     */
    LoadSummaryDTO loadForPeriod(int month, int year, Long userId);

    /**
     * Loads all active payroll components for a single employee in the given period.
     * Working days are resolved from the {@code PayrollPeriod} record for that month/year.
     *
     * @param empId  employee ID
     * @param month  payroll period month (1–12)
     * @param year   payroll period year
     * @param userId ID of the user triggering the load
     * @return summary of records processed and any non-fatal errors
     */
    LoadSummaryDTO loadForEmployee(Long empId, int month, int year, Long userId);
}
