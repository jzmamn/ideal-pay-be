# ADR-001: Generic Payroll Reporting Engine

**Status:** Proposed
**Date:** 2026-06-06
**Deciders:** Backend lead, Frontend lead, Product owner

## Context

We just shipped three payroll reports (Payroll Summary, Bank Transfer, No-Pay) using the established pattern in this codebase: one MySQL stored procedure per report (`sp_rpt_<name>`), one controller endpoint + service method per report in `PayrollPivotController`/`PayrollPivotService`, and a hardcoded `REPORT_DEFS` entry in the Angular `Reports` component that declares the report's columns/labels and a matching branch in `load()` that calls a dedicated `ReportsService` method.

This pattern works, but the team now wants payroll reporting to grow well beyond flat tables: filters (period, branch, department, employee status, etc.), CSV/Excel/PDF export, print-friendly views, charts (trend lines, breakdowns), and pivot views (cross-tabbing components like fixed allowances/deductions, similar to the existing `sp_emp_fa_pivot`-style procedures). Doing all of that by hand for every new report means N stored procedures, N controller endpoints, N frontend `REPORT_DEFS` entries, and N bespoke chart/pivot/export wiring — an increasingly expensive and inconsistent way to add reports.

**Forces at play:**
- The existing pivot procedures (`sp_emp_fa_pivot`, `sp_emp_np_pivot`, etc.) already follow a semi-generic "dynamic columns via stored procedure" idiom that a generic engine could standardize on.
- All current report SPs return `List<Map<String, Object>>` (column-name-keyed rows) — already a generic, engine-friendly shape.
- The frontend already centralizes report metadata in one `REPORT_DEFS` array and one `Reports` component/table — a natural seam to generalize.
- Constraint: minimize churn for the three reports just shipped; whatever we choose should let them be migrated incrementally, not rewritten from scratch.
- Non-functional requirements: payroll data is sensitive (must stay server-side filterable, not just client-filtered after a bulk fetch); reports must remain auditable (who ran what, with which filters); export/print must produce numbers that match what's on screen.

## Decision

Adopt a **config-driven generic report engine**: report *definitions* (id, label, data source, parameters/filters, columns, chart/pivot hints, export options) live in one place and are interpreted by one generic execution path on both backend and frontend, instead of being re-implemented per report. New reports become a registry entry plus a stored procedure — not new controller endpoints, services, or frontend components.

Concretely:

- **Backend:** introduce a `report_definition` (+ `report_parameter`) table seeded via Flyway, and a single generic endpoint `GET /payroll/reports/{reportKey}/run?<params>` backed by a `ReportEngineService` that looks up the definition, validates/binds parameters, and calls the configured stored procedure (still `sp_rpt_<name>`, still returning `List<Map<String, Object>>`) via `JdbcTemplate`. Existing per-report endpoints remain as thin deprecated wrappers during migration.
- **Frontend:** replace the hardcoded `REPORT_DEFS` + per-report `ReportsService` methods with a `ReportRegistryService` that fetches report metadata from `GET /payroll/reports` and a generic `ReportViewer` component that renders filters, table/pivot, charts, export, and print from that metadata — driven by declarative column/format/chart hints rather than bespoke code per report.
- **Cross-cutting:** add a generic `/payroll/reports/{reportKey}/run` audit log entry (who, when, filters) for compliance, and a shared export module (CSV now; Excel/PDF added behind the same interface) plus a print stylesheet applied to the generic viewer.

## Options Considered

### Option A: Keep the current per-report pattern (status quo)
| Dimension | Assessment |
|-----------|------------|
| Complexity | Low — no new abstractions, team already knows it |
| Cost | Low upfront, **high** marginal cost per report (SP + endpoint + service + frontend wiring + chart/export code, repeated) |
| Scalability | Poor — N reports → N nearly-identical code paths; chart/pivot/export logic gets copy-pasted and drifts |
| Team familiarity | High |

**Pros:** Zero migration risk; consistent with existing `PayrollPivotController` style; easy to reason about one report at a time.
**Cons:** Linear growth in boilerplate; charts/pivots/exports/printing each need to be re-solved per report; inconsistent UX likely as different developers implement "the same" feature slightly differently; harder to add cross-cutting concerns (audit logging, caching, access control) consistently.

### Option B: Config-driven generic report engine (recommended)
| Dimension | Assessment |
|-----------|------------|
| Complexity | Medium-high upfront (registry schema, generic execution + rendering pipeline); low per new report afterward |
| Cost | Higher initial investment, amortized quickly — the 4th–Nth report cost drops to "write an SP + add a registry row" |
| Scalability | Strong — charts, pivots, export, print, filters, audit logging are solved once and reused |
| Team familiarity | Medium — generic/metadata-driven UI is a new pattern for this codebase, but consistent with how `REPORT_DEFS` already centralizes metadata |

**Pros:** New reports become "mostly configuration"; consistent UX (filters, export, print, charts look and behave the same everywhere); cross-cutting concerns (audit, caching, RBAC on reports) implemented once; SP-per-report keeps SQL close to the data and DBA-reviewable; aligns with the existing `sp_rpt_` / `List<Map<String,Object>>` conventions, so it's additive rather than disruptive.
**Cons:** Requires designing a metadata schema that's flexible enough for pivots and charts without becoming its own mini-DSL; generic frontend rendering is harder to debug than a bespoke component; risk of over-engineering if report variety stays low; needs careful TypeScript typing to avoid `any` creeping in around dynamic column sets.

### Option C: Hybrid — generic engine for tabular/export/print/filter concerns, bespoke components for charts/pivots
| Dimension | Assessment |
|-----------|------------|
| Complexity | Medium — smaller generic surface area than full Option B |
| Cost | Medium — saves boilerplate on the "common 80%" (filter, table, export, print) while allowing custom code for genuinely unusual visualizations |
| Scalability | Good for tabular reports; chart/pivot reports still require some custom work, but less than today |
| Team familiarity | Medium — closer to current patterns, smaller leap |

**Pros:** Lower-risk first step; captures most of the boilerplate reduction (filters/table/export/print, which is ~80% of the current pain) without committing to a fully generic chart/pivot DSL up front; can evolve into Option B later if chart/pivot needs prove uniform enough to generalize.
**Cons:** Two patterns to maintain in parallel (generic core + bespoke visual extensions); some duplication remains for chart/pivot-heavy reports; defers (rather than solves) the long-term consistency problem.

## Trade-off Analysis

The central trade-off is **upfront design cost vs. long-term marginal cost**. Option A has the lowest risk today but its cost compounds with every new report — and the team has explicitly signalled it wants to add filters, charts, pivots, exports, and printing broadly, which is exactly the scenario where per-report bespoke code becomes unsustainable. Option C is the pragmatic middle ground: it removes the most repetitive boilerplate (filter UI, server-side filtering, table rendering, export, print) immediately, while deferring the harder design problem (a fully generic chart/pivot metadata model) until we've built a few more chart/pivot reports and can see the real shape of that variation.

Option B is the right end-state, but committing to a full generic chart/pivot DSL now — before we've built more than the existing FA/FD/NP pivots and zero charts — risks over-fitting the metadata schema to assumptions that don't hold once real chart requirements appear. Recommendation: **adopt Option B as the target architecture, but deliver it incrementally starting with the Option C slice** (generic filters/table/export/print engine first; metadata-driven charts and pivots as a fast-follow once 2–3 concrete chart/pivot reports exist to generalize from).

## Consequences

- **Becomes easier:** adding a new tabular report (registry row + SP); keeping export/print/filter UX consistent; auditing report usage centrally; enforcing access control per report.
- **Becomes harder:** debugging a specific report (one more layer of indirection — generic engine interpreting metadata vs. a dedicated component you can read top-to-bottom); onboarding (new devs must learn the metadata schema, not just Angular/Spring patterns); the metadata schema itself becomes a thing that needs versioning and migration discipline.
- **Will need to revisit:** the `report_definition` schema once real chart/pivot requirements exist (likely needs a `chart_config` / `pivot_config` JSON column or companion tables); whether server-side pagination/streaming is needed once reports run over full payroll history rather than a single month; whether the generic engine should support non-SP data sources (e.g., aggregations across multiple SPs) as needs grow.

## Action Items

1. [ ] Draft the `report_definition` / `report_parameter` schema (Flyway migration) and circulate for review — keep it additive alongside existing `sp_rpt_` procedures.
2. [ ] Build the generic `GET /payroll/reports` (list) and `GET /payroll/reports/{reportKey}/run` endpoints in a new `ReportEngineController`/`ReportEngineService`, with audit logging.
3. [ ] Build the frontend `ReportRegistryService` + generic `ReportViewer` (filters, table, CSV export, print stylesheet) and migrate Payroll Summary, Bank Transfer, and No-Pay onto it as the pilot reports.
4. [ ] Add Excel/PDF export behind the same export interface used for CSV.
5. [ ] Prototype one chart-based report and one pivot-based report on the engine; use the results to design the `chart_config`/`pivot_config` metadata extension (Option B fast-follow).
6. [ ] Deprecate the per-report controller endpoints (`/payroll/pivot/payroll-summary-report`, etc.) once the generic path is verified in production, keeping them only as compatibility shims until the frontend migration completes.
