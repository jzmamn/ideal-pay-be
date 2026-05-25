# Formula Definition Management UI — Angular Implementation Prompt

## Context

I have a Spring Boot payroll backend with a formula engine based on MVEL. I need an Angular component to manage formula definitions. The backend base URL is `/payroll`.

---

## Backend API reference

### Endpoints

| Method | URL | Purpose |
|--------|-----|---------|
| GET | `/payroll/formula?isActive=all&showDefaultRow=false` | List all formulas |
| GET | `/payroll/formula/{id}` | Get single formula |
| GET | `/payroll/formula/by-type?formulaType=OVERTIME&isActive=all` | Filter by type |
| POST | `/payroll/formula` | Create formula |
| PUT | `/payroll/formula/{id}` | Update formula |
| DELETE | `/payroll/formula/{id}` | Delete formula |
| POST | `/payroll/formula/evaluate` | Test a formula with values |
| POST | `/payroll/formula/validate` | Validate expression syntax only |

### Response wrapper
Every response is wrapped:
```json
{
  "success": true,
  "message": "...",
  "data": { }
}
```

### FormulaDefinition object
```typescript
interface FormulaDefinitionResponseDTO {
  id: number;
  code: string;
  name: string;
  description: string;
  formulaType: 'OVERTIME' | 'NO_PAY' | 'VARIABLE_ALLOWANCE' | 'VARIABLE_DEDUCTION';
  expression: string;
  isActive: boolean;
  createdById: number;
  createdByCode: string;
  createdByUserName: string;
  createdDate: string;
  modifiedById: number;
  modifiedByCode: string;
  modifiedByUserName: string;
  modifiedDate: string;
}
```

### Create / Update request body
```typescript
interface FormulaDefinitionRequestDTO {
  code: string;           // max 20 chars, unique
  name: string;           // max 100 chars
  description?: string;   // max 255 chars
  formulaType: 'OVERTIME' | 'NO_PAY' | 'VARIABLE_ALLOWANCE' | 'VARIABLE_DEDUCTION';
  expression: string;     // MVEL expression, validated server-side on save
  isActive: boolean;
  createdBy: number;      // user ID
  modifiedBy: number;     // user ID
}
```

### Evaluate request / response
```typescript
// POST /payroll/formula/evaluate
interface FormulaEvaluateRequestDTO {
  formulaId?: number;     // use saved formula, OR
  expression?: string;    // ad-hoc expression
  basicSalary?: number;
  workingDays?: number;   // default 26
  nopayDays?: number;     // default 0
  otHours?: number;
  otRate?: number;
  customVariables?: Record<string, number>;
}

interface FormulaEvaluateResponseDTO {
  expression: string;
  result: number | null;
  context: Record<string, any>;
  technicalError: string | null;   // raw MVEL error, for developers
  userFriendlyError: string | null; // plain-language error, show in UI
}
```

---

## Screen layout

### 1. Formula list (top of page)

- Page header: title "Formula definitions", subtitle "Manage MVEL expressions linked to overtime, allowances and deductions", and a **New formula** button (top right) that scrolls to the form and resets it.
- **Filter bar** above the table:
  - Dropdown: All types / Overtime / No-pay / Variable allowance / Variable deduction
  - Dropdown: All statuses / Active / Inactive
- **Table columns**: Code, Name, Type (badge), Expression (truncated monospace), Status (badge), Actions
- **Actions per row**: Edit button (loads row into form, switches to Details tab) and Test button (loads row into form, switches to Test formula tab)
- Type badge colors:
  - OVERTIME → blue
  - NO_PAY → amber
  - VARIABLE_ALLOWANCE → green
  - VARIABLE_DEDUCTION → red
- Active badge → green. Inactive badge → gray.

### 2. Create / Edit form (below the list)

A card with **three tabs**: Details · Test formula · Linked components.

---

#### Tab 1 — Details

Fields in a 2-column grid:

| Field | Type | Notes |
|-------|------|-------|
| Code | text input | max 20 chars, required |
| Formula type | select | OVERTIME / NO_PAY / VARIABLE_ALLOWANCE / VARIABLE_DEDUCTION, required |
| Name | text input (full width) | max 100 chars, required |
| Description | text input (full width) | max 255 chars, optional |
| Expression | textarea (full width, monospace) | required — see below |
| Status | select | Active / Inactive |
| Test expression | textarea (full width, monospace) | see below |

**Expression field behaviour:**
- Above the textarea: a dropdown labelled "Insert field" with three option groups:
  - *Standard payroll fields*: basicSalary, workingDays, nopayDays, otHours, otRate (each with a short description)
  - *Operators*: + · - · * · / · ( ) · ?: (ternary)
  - *Common constants*: 0.08 (EPF employee 8%), 0.12 (EPF employer 12%), 0.03 (ETF 3%), 0.10 (10%), 8 (hours per day)
- Selecting an option inserts its value at the current cursor position inside the textarea, then resets the dropdown.
- Selecting `( )` wraps any selected text in parentheses.
- Below the textarea: a live validation hint — on each keystroke call `POST /payroll/formula/validate` with the current expression; show a green tick + preview result (with default values) if valid, or a red error message if invalid.

**Test expression field** (below Status, inside a shaded box):
- Label: "Test expression". Subtitle: "Hardcode values directly to verify the result. This field is not saved."
- Same monospace textarea.
- A **Copy from expression** button that copies the Expression field content into this field.
- As the user types, evaluate locally (browser JS) and show a result box below:
  - Green background + result value on success.
  - Red background + `userFriendlyError` message on failure, with a **Show technical detail** toggle that reveals `technicalError` beneath it.
- Note: local evaluation is browser-only. For server-side validation use the Test formula tab.

**Action row** (bottom of tab):
- Clear button — resets the whole form.
- **Test with variables** button — switches to the Test formula tab.
- **Save formula** button — calls POST or PUT depending on whether an ID is loaded.

---

#### Tab 2 — Test formula

Calls `POST /payroll/formula/evaluate` with the expression from the Details tab.

Layout inside a shaded panel:

1. **Standard variables** — a 5-column grid of number inputs:
   - basicSalary (default 80000)
   - workingDays (default 26)
   - nopayDays (default 0)
   - otHours (default 12)
   - otRate (default 1.5)

2. **Custom variables** — a repeatable row of key + value inputs with an **Add variable** button and a remove (×) button per row. Used for custom formula variables like `performanceAmount` or `loanInstalment`.

3. **Result block** — shown after clicking Run:
   - Green background with the numeric result on success.
   - Red background with `userFriendlyError` on failure, and a **Show technical detail** toggle revealing `technicalError`.

4. A **Run** button that sends the request. Also auto-runs when any input changes.

5. A note at the bottom: "This evaluates on the server using MVEL. Use `/calculate` endpoints on individual components for production payroll runs."

---

#### Tab 3 — Linked components

Read-only list of Overtime / VariableAllowance / VariableDeduction / NopayDays records that have `formulaDefinitionId` set to the currently loaded formula's ID.

- Each row: component code + name, component type badge, "Linked" badge (green) or "Not linked" (gray).
- Populated by querying the relevant GET endpoints filtered by the loaded formula ID.
- A note: "To change the link, edit the component directly from its own screen."

---

## Standard variables reference (for tooltips / hints)

| Variable | Type | Description |
|----------|------|-------------|
| basicSalary | BigDecimal | Employee's basic salary |
| workingDays | Integer | Working days in the payroll period (default 26) |
| nopayDays | Integer | No-pay days taken by the employee |
| otHours | BigDecimal | Overtime hours worked |
| otRate | BigDecimal | Overtime multiplier (e.g. 1.5 for time-and-a-half) |

Custom variables are passed via `customVariables` in the evaluate request and must match variable names used in the expression exactly.

---

## Example expressions

```
// Standard overtime
(basicSalary / workingDays / 8) * otHours * otRate

// No-pay deduction
(basicSalary / workingDays) * nopayDays

// Attendance bonus — full only if no absent days
nopayDays == 0 ? basicSalary * 0.10 : 0

// Pro-rated attendance bonus
basicSalary * 0.10 * ((workingDays - nopayDays) / workingDays)

// EPF employee contribution
basicSalary * 0.08
```

---

## Error handling

- On save: if the server rejects the expression (HTTP 400), show the error message from the response body inline below the expression field.
- On evaluate: always show `userFriendlyError` prominently; `technicalError` behind a toggle.
- On network error: show a generic toast notification.

---

## What to generate

Please create a complete Angular standalone component (or module-based if the project uses modules) for this screen, including:

1. `formula-definition.component.ts` — component logic
2. `formula-definition.component.html` — template
3. `formula-definition.component.scss` (or `.css`) — styles
4. `formula-definition.service.ts` — HTTP service wrapping all the API calls above
5. TypeScript interfaces for all request / response DTOs

Follow the existing project conventions for HTTP calls, error handling, and form validation. Use Angular Reactive Forms for the main form. Use `HttpClient` for API calls. Do not add any new third-party dependencies beyond what Angular provides.
