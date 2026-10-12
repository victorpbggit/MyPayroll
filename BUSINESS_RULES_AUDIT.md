# BUSINESS_RULES_AUDIT.md — MyPayroll

- **Audit date:** 12-10-2026. Read-only. No calculator code was changed to produce this document.
- **Prerequisite reading:** `ARCHITECTURE_AUDIT.md` §6 establishes that there are **two separate, disconnected payroll formulas** in this codebase. Every rule below is evaluated against **both** paths separately, because they disagree.
  - **Path A ("persisted/official")** = `PreviewPayrollUseCase` → `ApprovePayrollUseCase`/`FinalizePayrollUseCase` → `PayrollResultEntity`. This is what a real, saved payroll run in this app actually uses today.
  - **Path B ("GP/TJ/UM/PH/UL/PL calculator")** = `CalculatePayrollUseCase` → `PayrollCalculator` (domain/payroll package), driven by `PayrollCalculatorScreen`/`PayrollCalculatorViewModel`. Correct per spec, but its output is **never persisted** — shown once, then discarded.

Status legend per the audit brief: `MATCH`, `MISMATCH`, `PARTIAL`, `NOT IMPLEMENTED`, `NOT VERIFIED`.

---

## Rule-by-rule comparison

### Manager: GP, TJ, UM, PH dibayar penuh per bulan

- **Path A:** `MISMATCH`. `PreviewPayrollUseCase.kt:43-54` has no `employeeType`/Manager branch of any kind — `basicSalary` is the flat latest salary-history value, `totalAllowance` is a flat sum of all active `EmployeePayComponentHistoryEntity` rows. There is no concept of "Manager" anywhere in this code path, because (per `ARCHITECTURE_AUDIT.md` §4.1) **no entity in the schema stores whether an employee is a Manager.**
- **Path B:** `MATCH`. `PayrollCalculator.kt:30-54`: `isManager = input.employeeType == EmployeePayrollType.MANAGER`; when true, `basicSalaryPaid = input.basicSalaryMonthly`, `positionAllowancePaid = input.positionAllowanceMonthly`, `mealAllowancePaid = input.mealAllowanceMonthlyManager`, `attendancePremiumPaid = input.attendancePremiumMonthlyManager` — all paid in full, unconditionally. Numeric example: GP=5,000,000, TJ=1,000,000, UM=500,000/mo, PH=300,000/mo, Manager → paid exactly 5,000,000 / 1,000,000 / 500,000 / 300,000 regardless of attendance.
- **Caveat on Path B:** `employeeType` is a per-calculation UI dropdown (`PayrollCalculatorScreen.kt`, defaults `NON_MANAGER`) with **no persisted link to the employee record** — the user must remember and correctly re-select Manager every time they calculate that employee, for every period. Not a formula bug, but an operational risk (wrong default, easy to forget).

### Non-Manager: GP dan TJ prorata (bulanan × hari hadir / hari kerja terjadwal)

- **Path A:** `NOT IMPLEMENTED`. No proration exists anywhere in `PreviewPayrollUseCase`.
- **Path B:** `MATCH`. `PayrollCalculator.kt:134-147` (`prorate`): `BigDecimal.valueOf(monthlyAmount).multiply(attendanceDays).divide(scheduledWorkDays, 0, RoundingMode.HALF_UP).longValueExact()`. **Rounding is HALF_UP to the nearest whole Rupiah, matching the stated expectation exactly.**
  - Numeric example: GP=5,000,000, attendanceDays=20, scheduledWorkDays=22 → `5,000,000 × 20 / 22 = 4,545,454.54…` → HALF_UP → **4,545,455**. Verified by hand against the formula; not run as an executable test (no unit test exists for this — see "Test coverage" below).
  - `scheduledWorkDays` and `attendanceDays` are plain `Int` fields typed into the calculator form — `scheduledWorkDays` is pre-filled from calendar days in the period (`period.endDate.toEpochDay() - period.startDate.toEpochDay() + 1`, `PayrollCalculatorViewModel.kt`), **not** from any real "scheduled work days" business concept (no such field exists in `AttendanceEntity` or anywhere else — confirmed in `ARCHITECTURE_AUDIT.md` §4.1). `attendanceDays` is pre-filled from real `AttendanceEntity` rows with status PRESENT/LATE within the period, then editable.
  - Validation: `PayrollCalculator.validate()` enforces `scheduledWorkDays > 0` and `attendanceDays in 0..scheduledWorkDays` (`PayrollCalculator.kt:187-191`), and throws `IllegalArgumentException` otherwise, surfaced to the UI as an error banner.

### Non-Manager: UM dan PH = tarif harian × hari hadir

- **Path A:** `NOT IMPLEMENTED`. No UM/PH-specific field exists; everything is folded into the single flat `totalAllowance` sum.
- **Path B:** `MATCH`. `PayrollCalculator.kt:44-54`: `mealAllowancePaid = multiplyExact(input.mealAllowanceDaily, attendanceDays, "Uang makan")`, same pattern for `attendancePremiumPaid`. Plain integer multiplication (no rounding needed — both operands are already whole numbers), using `Math.multiplyExact` with an `IllegalArgumentException` thrown on overflow rather than silent wraparound (`PayrollCalculator.kt:161-167`) — a correctness-over-convenience choice, `MATCH` and slightly more defensive than the bare requirement asks for.
  - Numeric example: UM daily rate 50,000 × 20 hari hadir = 1,000,000.

### UL per event: jam pertama 100%, jam kedua 150%, jam di atas 2 = 200%

- **Path A:** `NOT IMPLEMENTED`. `totalOvertimeAmount` is `sum(OvertimeRecordEntity.calculationAmount)`, and `calculationAmount` itself is set in `OvertimeViewModel.recordActual` as a **flat `minutes * ratePerMinute`**, where `ratePerMinute` is a plain number typed by the user with an explicit on-screen disclaimer ("Belum ada aturan tarif lembur resmi - isi manual sesuai kebijakan.") — **confirmed zero tiering anywhere in this path**, by direct code read and by a `grep -rniE "150|200|tier|multiplier"` sweep across `data/repository` and `domain/repository` returning no relevant hits.
- **Path B:** `MATCH`. `PayrollCalculator.kt:56-93`, per overtime event: hour 1 = `multiplyExact(rate, min(hours,1), ...)` (100%); hour 2 = `calculateOvertimeSegment(rate, min(max(hours-1,0),1), 150)`; hours 3+ = `calculateOvertimeSegment(rate, max(hours-2,0), 200)`. `calculateOvertimeSegment` (`PayrollCalculator.kt:149-159`) does `rate × hours × percentage / 100` via `BigDecimal`, `RoundingMode.HALF_UP`.
  - Numeric example, UL rate = 20,000/hour, one event of 4 hours: hour1 = 20,000×1×100%=20,000; hour2 = 20,000×1×150%=30,000; hours 3–4 (2 remaining hours) = 20,000×2×200%=80,000. Total UL for that event = **130,000**. Matches the spec's stated tiers exactly.
  - Overtime events are entered as a UI-only list (`OvertimeEventDraft`, hours + optional PL) inside the calculator form, **not sourced from `OvertimeAssignmentEntity`/`OvertimeRecordEntity` at all** — the real, persisted overtime records (hours worked, approved/rejected) play no role in Path B's inputs; the user re-types hours from memory/paper when using the detailed calculator. This is a data-integrity gap: the two overtime tracking mechanisms (real `OvertimeRecordEntity` rows vs. the calculator's manual `OvertimeEventDraft` list) are completely independent, so there is no guarantee the numbers typed into the calculator match what was actually approved in the Overtime screen.

### PL: dibayar satu kali per event jika durasi > 2 jam

- **Path A:** `NOT IMPLEMENTED`. No PL concept exists in `OvertimeRecordEntity`/`PreviewPayrollUseCase` at all.
- **Path B:** `MATCH`. `PayrollCalculator.kt:85-92`: `if (hours > 2) { overtimePremium = addExact(overtimePremium, event.overtimePremiumPerEvent, ...) }` — strictly `> 2`, not `>= 2`, matching the spec. The UI (`PayrollCalculatorScreen.kt`'s `AddOvertimeEventDialog`) only shows/enables the PL input field once the typed hours exceed 2, and defaults it to `0` unless Payroll Setup's PL default (added this session) is pre-filled.
  - Numeric example: event of exactly 2 hours → PL = 0 (boundary correctly excluded); event of 2.0001→3 hours (integer hours, so 3h) → PL = the configured per-event amount, paid once regardless of how far past 2 hours it goes.

### Additional Earnings harus bisa ditambahkan

- **Path A:** `NOT IMPLEMENTED` as a generic concept. The closest equivalent is `EmployeeIncentiveEntity` (date + fixed amount, tied to a `PayComponentEntity`) and `EmployeePayComponentHistoryEntity` (effective-dated amount per component) — both real, persisted, and summed into `totalIncentive`/`totalAllowance` respectively. But there is no free-form "add an arbitrary extra earning for this payroll run" concept in the persisted path.
- **Path B:** `PARTIAL`. `PayrollCalculator.kt:95-98,213-217`: `additionalEarnings: List<AdditionalEarning>` (name + non-negative amount), validated and summed into `totalEarnings`. Fully correct as a pure-function feature, but entered through `PayrollCalculatorScreen`'s `NamedAmountEditor`/`AddNamedAmountDialog` as **ephemeral in-memory state** (`PayrollCalculatorFormState.additionalEarnings`) — nothing is written to any table; the list is lost the moment the user navigates away from the calculator screen. `PARTIAL` because the calculation support is correct but there is zero persistence.

### Deductions dapat mencakup penalty/warning, kasbon, dan potongan lain; berlaku untuk Manager maupun Non-Manager

- **Kasbon (cash advance/loan):** `NOT IMPLEMENTED`, full stop. `grep -rniE "kasbon|loan|advance|pinjaman|cash.?advance"` across every entity and DAO file returns **zero matches**. There is no schema concept of an employee loan/advance anywhere in this codebase, in either payroll path.
- **Penalty/warning:** `EmployeeDisciplinaryActionEntity` exists and is a real, persisted record (incident date, action type enum VERBAL_WARNING…SP3, free-text category/description) via a dedicated Discipline screen — but it has **no monetary field** (confirmed field-by-field, `ARCHITECTURE_AUDIT.md` §4.1) and, critically, **`PreviewPayrollUseCase` never reads from `EmployeeDisciplinaryActionRepository` at all** (confirmed: the repository is referenced only in its own interface/impl/DI-wiring files, nowhere in the payroll pipeline). So disciplinary actions are logged but have **zero effect on any payroll calculation, in either path.** `NOT IMPLEMENTED` as a deduction mechanism, despite being a fully functional record-keeping feature in its own right.
- **"Potongan lain" (generic deductions):** Path A has only `attendanceDeduction` (late/absent, policy-rate-driven) + `bpjsDeduction`/`taxDeduction` (statutory, also policy-rate-driven, default 0). Path B has a free-form `deductions: List<PayrollDeduction>` (name+amount) — same ephemeral, non-persisted pattern as Additional Earnings (`PARTIAL`, same reasoning).
- **Applies to Manager and Non-Manager alike:** `MATCH` for Path B specifically — `PayrollCalculator.kt` applies `deductionsTotal` uniformly after computing `totalEarnings`, with no Manager/Non-Manager branch in the deduction logic (per the spec, deductions aren't supposed to differ by employee type) — `require(deductionsTotal <= totalEarnings)` guards against a negative THP (`PayrollCalculator.kt:116-118`). Not applicable to Path A since neither penalty nor kasbon exist there at all.

### THP = Total Earnings − Total Deductions

- **Path A:** `MATCH` structurally. `PayrollLineItem.kt:14-16`: `grossPay = basicSalary+totalAllowance+totalIncentive+totalOvertimeAmount`; `totalDeduction = attendanceDeduction+bpjsDeduction+taxDeduction`; `netPay = grossPay - totalDeduction`. The *structure* (earnings minus deductions) matches; the *components feeding it* do not implement GP/TJ/UM/PH/UL/PL (see rules above) — so this is "the right shape, wrong ingredients."
- **Path B:** `MATCH`, both structurally and in component correctness. `PayrollCalculator.kt:104-131`: `totalEarnings` = sum of (prorated/full GP, TJ, UM, PH, UL, PL, additional earnings); `takeHomePay = totalEarnings - deductionsTotal`.

### Nilai uang Long rupiah; prorata HALF_UP

- `MATCH` for both paths on the "Long rupiah, no floating point for money" rule — every entity/domain model field for money is `Long` (confirmed throughout `ARCHITECTURE_AUDIT.md` §4.1 and the full entity sweep). The one exception: `PreviewPayrollUseCase.kt:77-78` computes `bpjsDeduction`/`taxDeduction` via `(grossBeforeStatutory * policy.bpjsEmployeeRatePercent / 100.0).toLong()` — this introduces a `Double` intermediate and **truncates** (`Double.toLong()` truncates toward zero, it does not round) rather than rounding HALF_UP. `MISMATCH` on the rounding sub-rule, scoped narrowly to these two statutory fields in Path A. Practically low-impact today because both policy rates default to `0.0` and are opt-in per preview run, but the rounding method itself is wrong relative to the stated HALF_UP expectation if/when a company actually configures a nonzero BPJS/tax rate.
- Path B's proration and overtime-tier math both correctly use `BigDecimal`/`RoundingMode.HALF_UP` (cited above) — `MATCH`.

### Pajak/BPJS tidak boleh dianggap otomatis dihitung kecuali benar-benar diimplementasikan sebagai komponen potongan

- `MATCH`. `PayrollPolicy.kt:11-23` explicitly documents that BPJS/tax/late/absent rates are **not** official confirmed company rules, default to `0` (no effect), and carries a user-facing `DISCLAIMER` string shown in the UI (`PayrollPeriodDetailScreen.kt`'s `PolicyDialog`) before every preview calculation. Nothing in either path computes tax/BPJS unless the user explicitly types a nonzero rate into that dialog for that specific preview run — rates are **not** saved as a standing policy (re-entered, defaulted to `"0"`, every single preview).

## Validation & edge-case behavior

| Concern | Path A (`PreviewPayrollUseCase`) | Path B (`PayrollCalculator`) |
|---|---|---|
| Negative amounts | No explicit guard; relies on upstream data (salary/incentive/pay-component entries) never being negative at entry — `NOT VERIFIED` whether any UI lets a negative `Long` into those tables | Explicit: `require(amounts.all { it >= 0L })` on all core amounts and on every named earning/deduction (`PayrollCalculator.kt:194-223`) |
| `attendanceDays` out of range | N/A (no proration happens) | Explicit `require(input.attendanceDays in 0..input.scheduledWorkDays)` (`PayrollCalculator.kt:190`) |
| `scheduledWorkDays <= 0` | N/A | Explicit `require(input.scheduledWorkDays > 0)` (`PayrollCalculator.kt:187`), avoids divide-by-zero in `prorate()` |
| Deductions exceeding earnings | No guard — `netPay` can go negative silently | Explicit `require(deductionsTotal <= totalEarnings)` (`PayrollCalculator.kt:116`), throws rather than producing a negative THP |
| Overflow | Plain `Long` arithmetic, no overflow guard | `Math.multiplyExact`/`Math.addExact` throughout, converted to a user-facing `IllegalArgumentException` rather than silently wrapping (`PayrollCalculator.kt:161-175`) |
| Zero/empty overtime events | Handled trivially (empty list → 0 overtime) in both paths | Same |
| Re-running Approve on the same period | Explicit upsert-by-(period,employee) with a DB unique index backstop (`ApprovePayrollUseCase.kt:31-48`, `PayrollResultEntity` unique index) — re-approving updates existing rows rather than duplicating | N/A — Path B never persists |
| Approving/finalizing an already-finalized period | Explicit `check(period.status != FINALIZED)` in `ApprovePayrollUseCase.kt:28` / `check(period.status == APPROVED)` in `FinalizePayrollUseCase.kt:25` — throws, UI surfaces as error banner | N/A |

**Path B is materially more defensive than Path A** — ironic, since Path B's output is the one that never gets saved.

## Test coverage

`NOT VERIFIED → confirmed absent.` `find app/src/test app/src/androidTest -type f` returns exactly two files: `ExampleUnitTest.kt` (the unmodified Android-Studio-template `assertEquals(4, 2+2)`) and `ExampleInstrumentedTest.kt` (template). **Zero unit tests exist for `PayrollCalculator`, `PreviewPayrollUseCase`, or any other business rule in this codebase.** `./gradlew testDebugUnitTest` was run during this audit (`CONFIRMED BY BUILD/TEST`: BUILD SUCCESSFUL, 12-10-2026) — it passes only because the one test it runs is trivial, not because any payroll rule was exercised.

## Summary status table

| # | Rule | Path A | Path B |
|---|---|---|---|
| 1 | Manager GP/TJ/UM/PH full pay | MISMATCH | MATCH |
| 2 | Non-Manager GP/TJ prorata | NOT IMPLEMENTED | MATCH |
| 3 | Non-Manager UM/PH daily×attendance | NOT IMPLEMENTED | MATCH |
| 4 | UL tiered 100/150/200% | NOT IMPLEMENTED | MATCH |
| 5 | PL once if >2h | NOT IMPLEMENTED | MATCH |
| 6 | Additional Earnings | NOT IMPLEMENTED | PARTIAL (correct math, not persisted) |
| 7 | Deductions incl. penalty/kasbon, both employee types | NOT IMPLEMENTED (kasbon: nowhere; penalty: logged but not linked) | PARTIAL (correct math, not persisted; kasbon absent everywhere) |
| 8 | THP = Earnings − Deductions | MATCH (structure only — wrong ingredients) | MATCH |
| 9 | Long rupiah / HALF_UP rounding | MATCH money-type; MISMATCH rounding on bpjs/tax (truncation) | MATCH |
| 10 | Tax/BPJS not auto-applied unless configured | MATCH | N/A (Path B has no statutory deduction concept at all) |
