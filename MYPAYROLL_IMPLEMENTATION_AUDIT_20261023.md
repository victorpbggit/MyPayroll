# MyPayroll --- Read-only Implementation Audit

-   **Audit date:** 23 October 2026
-   **Input:** `Payroll.zip` uploaded in this conversation
-   **Scope:** Read-only inspection of Kotlin source and existing
    project audit documents.
-   **Changes made:** None. No application source, configuration, DAO,
    Entity, database migration, or tests were modified.
-   **Evidence rule:** `CONFIRMED` means present in the uploaded source;
    `MISSING` means not found in the inspected source; `NOT RUN` means
    no build/test/runtime command was executed in this audit.

## Executive summary

The ZIP contains the Android project, existing audit documents, and
source code. The main risk remains the official/persisted payroll
calculation path: it does not use the detailed `PayrollCalculator` and
therefore does not implement the newly agreed payroll rules. Do not
deploy payroll results from this implementation as if the business rules
were already implemented.

## Findings

  --------------------------------------------------------------------------------------------------------------------------
  ID             Area             Status         Evidence from uploaded source                    Risk / implication
  -------------- ---------------- -------------- ------------------------------------------------ --------------------------
  A-01           Official payroll **MISSING /    `domain/usecase/PreviewPayrollUseCase.kt`        Official payroll preview
                 calculation      P0**           calculates basic salary, incentives, stored      and saved results do not
                                                 overtime amount and deductions directly. It sets follow the agreed
                                                 `totalAllowance = 0L`; it does not call          component formulas.
                                                 `PayrollCalculator`.                             

  A-02           Detailed         **PARTIAL /    `domain/payroll/PayrollCalculator.kt` exists,    Conflicts with agreed
                 calculator       P0**           but its current logic uses progressive tiers     rules: actual-minute
                                                 (first hour 100%, second 150%, later hours       duration, whole-duration
                                                 200%), PL per event when `hours > 2`, whole      150% below 2 hours / 200%
                                                 hours as `Int`, and `RoundingMode.HALF_UP`.      at or above 2 hours, PL
                                                                                                  once per day when combined
                                                                                                  duration ≥2 hours, and GL
                                                                                                  decimals truncated.

  A-03           Employee         **MISSING /    `EmployeeEntity.kt` has no Manager/Non-Manager   The official payroll path
                 classification   P0**           classification. `PayrollCalculationInput.kt` has cannot reliably select the
                                                 a transient `EmployeePayrollType`, but it is     correct employee-type
                                                 only an input to the separate calculator.        rule.

  A-04           Overtime source  **MISSING /    `presentation/overtime/OvertimeViewModel.kt`,    Stored overtime amounts
                 amount           P0**           `recordActual(...)` calculates                   use a manually supplied
                                                 `calculationAmount = minutes * ratePerMinute`.   flat per-minute rate, not
                                                                                                  the agreed UL formula and
                                                                                                  threshold.

  A-05           Overtime rules   **PARTIAL /    `OvertimeRuleConfig` and Payroll Setup UI exist. UI settings may not affect
                 setup            P1**           Its own source comment says the stored           official payroll
                                                 percentages are not consumed by the              calculations.
                                                 calculator/use case.                             

  A-06           PL default and   **MISSING /    No persisted employee UL/PL compensation fields  Agreed UL default = GPPJ,
                 persistence      P0**           were found in the inspected `EmployeeEntity` or  editable UL, PL default =
                                                 `PayrollResultEntity`; detailed calculator       UL, independent edit, and
                                                 accepts PL per overtime event as transient       daily aggregation are not
                                                 input.                                           represented in the
                                                                                                  official persisted payroll
                                                                                                  path.

  A-07           Payroll workflow **PARTIAL /    `PayrollPeriodStatus` is `OPEN`, `PREVIEWED`,    No separate `PAID` status
                 states           P1**           `APPROVED`, `FINALIZED`, `CANCELLED`;            or payment-confirmation
                                                 `ApprovePayrollUseCase` saves results and sets   field was found; current
                                                 `APPROVED`; `FinalizePayrollUseCase` locks       workflow cannot represent
                                                 results as `FINALIZED`.                          the agreed "operator marks
                                                                                                  paid after transfer" event
                                                                                                  distinctly.

  A-08           CEO approval     **MISSING /    Payroll detail UI allows selection of an         The system records an ID
                 enforcement      P1**           employee under "Disetujui oleh";                 but does not enforce that
                                                 `ApprovePayrollUseCase` accepts nullable         it is the CEO.
                                                 `approverId`. No login/role implementation was   
                                                 found in inspected Kotlin source.                

  A-09           Incomplete       **MISSING /    `FinalizePayrollUseCase` checks that period      A payroll may be finalized
                 attendance       P1**           status is `APPROVED`, then finalizes the saved   without the agreed
                 blocks                          results. No validation of completeness of        completeness gate.
                 finalization                    required attendance/overtime inputs was found    
                                                 there.                                           

  A-10           Holiday calendar **PARTIAL /    `PayrollCalendarRepositoryImpl` stores global    Manual calendar exists,
                                  P1**           date overrides in `SharedPreferences`; Sunday    but no check was found
                                                 defaults to holiday. Calendar UI allows manual   preventing edits to dates
                                                 edits.                                           used by a locked payroll
                                                                                                  period. No connection was
                                                                                                  found that automatically
                                                                                                  classifies all holiday
                                                                                                  work hours as overtime in
                                                                                                  the official payroll
                                                                                                  calculation.

  A-11           THP Advance      **PARTIAL /    The official preview uses dated                  The agreed declaration
                                  P1**           `EmployeeIncentiveEntity` amounts; the separate  vs. realization flow is
                                                 calculator supports transient                    not implemented end-to-end
                                                 `AdditionalEarning` entries. No per-component    in official payroll.
                                                 CEO realization workflow or persistent           
                                                 payroll-period snapshot for THP Advance was      
                                                 found in the inspected official path.            

  A-12           Payroll          **MISSING /    No dedicated correction request                  Mandatory reason, CEO
                 corrections      P1**           workflow/entity/use case was found among the     approval/revision,
                                                 inspected source paths.                          multi-employee/component
                                                                                                  correction, and
                                                                                                  under/overpayment handling
                                                                                                  are not represented.

  A-13           Audit log        **MISSING /    No `AuditLog` entity/repository/use case or      Changes to compensation,
                                  P1**           equivalent audit-log implementation was found in attendance, payroll,
                                                 inspected Kotlin source.                         approval, correction and
                                                                                                  configuration are not
                                                                                                  comprehensively traceable.

  A-14           Authentication   **MISSING /    No login, role, or permission implementation was Administrator, Payroll
                 and role access  P1**           found in inspected Kotlin source.                Operator and CEO
                                                                                                  permissions are not
                                                                                                  enforced by the
                                                                                                  application.

  A-15           Payroll rounding **CONFLICT /   `PayrollCalculator.prorate()` and                Inconsistent rounding
                                  P1**           `calculateOvertimeSegment()` use `HALF_UP`;      behavior; only GL
                                                 official path uses `toLong()` for statutory      truncation is currently
                                                 deductions, truncating those particular results. agreed, while other
                                                                                                  rounding rules remain
                                                                                                  open.

  A-16           Payroll tests    **PARTIAL**    Test source includes tests for late penalty,     Critical calculation rules
                                                 overtime config, and calendar rules; no          and boundary cases lack
                                                 `PayrollCalculator` unit test file was found.    dedicated tests.
  --------------------------------------------------------------------------------------------------------------------------

## Protected packages

The following packages were inspected read-only and were **not
modified**: - `com.mypayroll.data.local.dao` -
`com.mypayroll.data.local.entity`

The audit suggests future data-model changes may be required (employee
classification, persisted compensation components, payment state,
correction requests, audit log). Per the project constraint, do not
change DAO/Entity files or queries until the exact migration is proposed
and explicitly authorized.

## Confirmed existing structure worth reusing

-   `PayrollPeriodEntity` and `PayrollResultEntity` already persist
    payroll periods/results.
-   `PayrollApprovalEntity` already records approval/finalization
    actions.
-   `PayrollCalendarRepositoryImpl` already supports manual global
    calendar overrides.
-   `OvertimeAssignmentEntity` and `OvertimeRecordEntity` already
    distinguish planned assignment from actual overtime record.
-   `PayrollCalculator` is pure domain logic and can be tested
    independently, but its formulas currently conflict with the latest
    business decisions.
-   Existing source includes Room migrations and project audit
    documents. They should be reviewed before proposing any schema
    change.

## Not verified in this audit

-   No Gradle build or unit test command was executed during this
    read-only review.
-   The app was not launched on a device/emulator.
-   Runtime behavior and current database migration success are
    therefore **not verified**.

## Recommended order of work

1.  **Do not edit source yet.** Confirm the data-model/migration
    proposal first because employee classification and UL/PL persistence
    are missing from the official path.
2.  Design one official calculation pipeline that uses the agreed domain
    rules and persists the exact same results it previews.
3.  Define the minimal schema migration and impact. Stop for explicit
    approval before changing protected DAO/Entity packages.
4.  Implement actual-minute overtime, daily aggregation for PL, UL/PL
    defaults and overrides, GV component reconciliation, and monthly
    vs. daily/weekly employee rules.
5.  Add the CEO review/THP Advance realization stage, payroll
    completeness gate, approval and payment separation, and correction
    workflow.
6.  Implement role enforcement and immutable/tamper-evident audit
    logging.
7.  Add automated tests for threshold boundaries, truncation,
    monthly/daily payroll, holiday work, THP Advance, corrections, and
    state transitions.
8.  Run Gradle tests/build and record actual results before calling the
    changes complete.
