# AS_BUILT_PRD.md — MyPayroll

- **Audit date:** 12-10-2026. Read-only reverse-engineered PRD. No source/config was changed to produce this document.
- **Companion documents:** `ARCHITECTURE_AUDIT.md` (layer/package/DB detail), `UI_UX_FLOW_AUDIT.md` (screen-by-screen), `BUSINESS_RULES_AUDIT.md` (formula-by-formula), `GAP_ANALYSIS.md` (prioritized punch list). This document is the executive-level synthesis; detailed evidence lives in those four files.
- Every section below separates **REQUIREMENTS / EXPECTATIONS** (what was asked of the app, as known to this audit — from the user's payroll-rules prompt and from the shape of the existing feature set) from **AS-BUILT BEHAVIOR** (what the code actually does, confirmed by reading it) and **GAP / NOT IMPLEMENTED** (the delta). Where a requirement was never explicitly stated to this audit, it is marked `OPEN QUESTION` rather than invented.

---

## 1. Executive summary

MyPayroll is a single-module, offline-only Android app (Kotlin, Jetpack Compose, Room, no backend) for a small/medium company to track employees, attendance, leave, overtime, organizational structure, documents, and payroll. It is functionally broad — 14 top-level screens plus several employee-scoped tabs — and the non-payroll CRUD features (Employee, Attendance, Leave, Overtime assignment/approval, Organization, Document metadata, Disciplinary log) are **largely complete and working**: every button/form checked in this audit reaches a real Room-backed repository, not a mock or hardcoded value.

**The one critical defect found is architectural, not cosmetic:** the app contains two separate, non-interacting payroll calculation implementations. The one that actually gets approved, finalized, and persisted to the database (`PreviewPayrollUseCase` → `PayrollResultEntity`) does **not** implement the Manager/Non-Manager, GP/TJ-proration, UM/PH, tiered-UL, or PL rules the user specified. A second, mathematically correct implementation of exactly those rules exists (`PayrollCalculator`, exposed via a "Kalkulator Detail per Karyawan" screen added in an earlier session and extended with default-value configuration in this session's "Payroll Setup" feature) — but its output is explicitly never saved anywhere. See `ARCHITECTURE_AUDIT.md` §6 and `GAP_ANALYSIS.md` ID-01.

Build status: `assembleDebug` succeeds and produces an installable debug APK (`CONFIRMED BY BUILD/TEST`, run 12-10-2026). Automated test coverage for business rules is effectively zero (only the unmodified Android Studio template test exists).

## 2. Product purpose and target user

**AS-BUILT BEHAVIOR (inferable from the feature set, not from an explicit product brief):** A single-tenant, single-device HR/payroll tool for one company's HR/admin staff to: maintain an employee roster and org structure; record daily attendance and exceptions; manage leave requests; assign and record overtime; track contracts, documents, and disciplinary actions; and run a monthly payroll cycle (preview → approve → finalize) per period.

**OPEN QUESTION:** No in-repo product brief, README, or requirements doc exists (confirmed — only this session's own `mod-121026-1.md` existed before this audit). The target company size, whether multiple HR users/roles are expected, and whether this is meant to run on one admin's phone/tablet vs. be shared, are not stated anywhere in the code and must be confirmed with the project owner.

## 3. Scope of features (as-built)

| Area | Present? |
|---|---|
| Employee directory + profile hub + edit | Yes |
| Organization (Department/Position/ContractType/Employee Contracts/Position history) | Yes |
| Attendance (daily entry + exceptions) | Yes |
| Leave requests + approval | Yes (no balance/quota tracking) |
| Overtime (assignment → accept/reject → record actual → approve) | Yes (flat manual rate, no tiering) |
| Document metadata | Yes (metadata only, no file storage) |
| Disciplinary action log | Yes (no monetary linkage) |
| Payroll period lifecycle (OPEN→PREVIEWED→APPROVED→FINALIZED) | Yes |
| Payroll preview/approve/finalize (persisted) | Yes, but wrong formula (see §11) |
| Payroll detailed calculator (GP/TJ/UM/PH/UL/PL) | Yes, correct formula, **not persisted** |
| Payroll Setup (default rates for GP/TJ/UM/PH/UL/PL) | Yes (added this session), feeds only the non-persisted calculator |
| PayComponent definitions CRUD | Yes, but unreachable from any navigation path |
| Dashboard | Yes, real aggregated data |
| Authentication / user roles | Not present |
| Kasbon / employee loan tracking | Not present anywhere in the schema |

## 4. Requirements known to this audit

**REQUIREMENTS / EXPECTATIONS**, verbatim from the payroll-rules prompt given to this audit (the authoritative source for business rules — everything below is directly comparable against `BUSINESS_RULES_AUDIT.md`):

- GP (Gaji Pokok), TJ (Tunjangan Jabatan), UM (Uang Makan), PH (Premi Hadir), UL (Upah Lembur), PL (Premi Lembur) as the six payroll components.
- Manager: GP/TJ/UM/PH paid in full monthly; deductions still reduce THP.
- Non-Manager: GP/TJ prorated (monthly × attendanceDays/scheduledWorkDays); UM/PH = daily rate × attendanceDays.
- UL tiered per overtime event: hour 1 = 100%, hour 2 = 150%, hours beyond 2 = 200% of the hourly rate.
- PL: paid once per overtime event if that event's duration exceeds 2 hours.
- Additional Earnings must be addable "sesuai dukungan data model."
- Deductions can include penalty/warning, kasbon, and other deductions, applicable to both employee types.
- THP = Total Earnings − Total Deductions.
- Money is `Long` Rupiah; proration rounds HALF_UP to the nearest Rupiah.
- Tax/BPJS must never be silently auto-calculated unless genuinely implemented as a deduction component.
- A Payroll Setup UI for configuring GP/TJ/UM/PH/UL/PL (this session's deliverable, now present).

No other product requirement (role model, multi-company support, reporting/export, notifications, etc.) was stated to this audit; anything not in this list that might look like a "missing feature" below is flagged `OPEN QUESTION`, not assumed.

## 5. Current feature behavior (AS-BUILT BEHAVIOR)

See `UI_UX_FLOW_AUDIT.md` for the full per-screen table. Summary: every CRUD feature outside payroll (Employee/Attendance/Leave/Overtime-assignment/Organization/Document/Discipline) reads and writes real Room data with explicit loading/empty/error states and no fabricated numbers. The one deliberately-not-faked gap is the Document feature's missing file storage (self-disclosed to the user in the UI text itself) and Leave's missing balance tracking (also deliberate, per code comment). The payroll area has the two-path problem described in §1 and detailed in §11.

## 6. User roles and Manager/Non-Manager

**REQUIREMENTS:** The payroll rules fundamentally depend on knowing whether each employee is a Manager or Non-Manager.

**AS-BUILT BEHAVIOR:** **GAP.** No such attribute is persisted anywhere in the schema (`EmployeeEntity`, `PositionEntity`, and every other entity were checked field-by-field — confirmed in `ARCHITECTURE_AUDIT.md` §4.1). The only place "Manager" exists at all is a transient UI dropdown (`EmployeePayrollType`, default `NON_MANAGER`) inside the never-persisted detailed calculator screen, re-selected from scratch by the user every single time they calculate a given employee's pay for a given period. There is also no app-level user-role/permission model of any kind (no login, no "HR admin" vs "approver" distinction) — `OPEN QUESTION` whether that was ever intended as a requirement.

## 7. User journeys

Walking the 8 journeys named in the audit brief, each step graded by evidence found:

1. **Buka aplikasi** → `MainActivity` → Dashboard, no gate. **WORKING.**
2. **Tambah/edit karyawan** → Employee list FAB → real insert; Employee Detail edit icon → real update. **WORKING.**
3. **Catat absensi** → Attendance screen, load employees as drafts → "Simpan N Absensi" → real upsert. **WORKING.**
4. **Catat lembur** → Overtime screen, assign → accept → record actual (flat rate × minutes, no tiering) → approve. **WORKING, but the persisted number does not use the UL/PL formula** (see §11).
5. **Setup komponen gaji** → Payroll Setup screen (new this session) → save 8 default rates to SharedPreferences. **WORKING** as a config screen; **does not reach the persisted payroll pipeline at all** — it only pre-fills the separate, non-persisted detailed calculator.
6. **Hitung payroll** → Two divergent paths exist from here:
   - a) Payroll period detail → "Hitung Pratinjau" (Path A, persisted-eligible, wrong formula) → "Setujui & Simpan". **WORKING mechanically, WRONG formula.**
   - b) Payroll period detail → "Kalkulator Detail per Karyawan" (Path B, correct GP/TJ/UM/PH/UL/PL formula) → "Hitung". **WORKING formula, output discarded, never reaches step 7/8.**
7. **Periksa detail dan potongan** → `PayrollPeriodDetailScreen`'s preview/saved-results list shows basicSalary/allowance/incentive/overtime/deduction/gross from Path A only — a user cannot see a persisted UM/PH/UL/PL breakdown anywhere, because that breakdown is never saved. **PARTIAL.**
8. **Finalisasi/riwayat payroll** → "Setujui & Simpan" → "Finalisasi" → period locked, `PayrollResultEntity` rows flipped to FINALIZED, immutable snapshot from then on. **WORKING** for Path A's (wrong) numbers.

## 8. Functional requirements matrix

| Requirement | Status | Evidence |
|---|---|---|
| Employee CRUD | DONE | `UI_UX_FLOW_AUDIT.md` §2 |
| Manager/Non-Manager classification (persisted) | NOT DONE | `ARCHITECTURE_AUDIT.md` §4.1, §6 |
| Attendance recording | DONE | `UI_UX_FLOW_AUDIT.md` §2 |
| Scheduled work days (as a real stored concept) | NOT DONE (derived from calendar days in a period, not a real attribute) | `BUSINESS_RULES_AUDIT.md` rule 2 |
| Overtime assignment/recording | DONE (mechanically) | `UI_UX_FLOW_AUDIT.md` §2 |
| GP rule | PARTIAL (correct in Path B only) | `BUSINESS_RULES_AUDIT.md` rules 1–2 |
| TJ rule | PARTIAL (correct in Path B only) | same |
| UM rule | PARTIAL (correct in Path B only) | same |
| PH rule | PARTIAL (correct in Path B only) | same |
| UL tiered rule | PARTIAL (correct in Path B only) | `BUSINESS_RULES_AUDIT.md` rule 4 |
| PL >2h rule | PARTIAL (correct in Path B only) | `BUSINESS_RULES_AUDIT.md` rule 5 |
| Payroll Setup persistence | DONE (SharedPreferences, confirmed round-trip) | `mod-121026-1.md`, this audit's direct code re-read |
| Additional Earnings | PARTIAL (correct math, zero persistence) | `BUSINESS_RULES_AUDIT.md` rule 6 |
| Deductions / penalty / warning | NOT DONE as a payroll-affecting mechanism | `BUSINESS_RULES_AUDIT.md` rule 7 |
| Kasbon | NOT DONE (no schema concept anywhere) | `BUSINESS_RULES_AUDIT.md` rule 7 |
| THP calculation | PARTIAL (right structure both paths; right ingredients only in Path B) | `BUSINESS_RULES_AUDIT.md` rule 8 |
| Payroll approval/finalization workflow | DONE (mechanically correct, wrong underlying formula) | `BUSINESS_RULES_AUDIT.md`, `UI_UX_FLOW_AUDIT.md` |
| Payroll historical snapshot (finalized immutability) | DONE | `ApprovePayrollUseCase`/`FinalizePayrollUseCase` checks, `ARCHITECTURE_AUDIT.md` §5 |
| Dashboard | DONE | `UI_UX_FLOW_AUDIT.md` §2 |
| Navigation (single NavHost, no second graph) | DONE | `ARCHITECTURE_AUDIT.md` §2, `UI_UX_FLOW_AUDIT.md` §1 |
| Database persistence (Room, non-payroll features) | DONE | `ARCHITECTURE_AUDIT.md` §4 |
| Validation/error handling | PARTIAL (strong in `PayrollCalculator`, weak/absent in `PreviewPayrollUseCase`) | `BUSINESS_RULES_AUDIT.md` "Validation & edge-case behavior" |
| Unit tests for payroll rules | NOT DONE | `BUSINESS_RULES_AUDIT.md` "Test coverage" |
| Build status | DONE (builds and installs) | §13 below |

## 9. Non-functional concerns

- **Reliability:** No `fallbackToDestructiveMigration()` — a future unmigrated schema change crashes the app rather than silently losing data (safer failure mode, but an ops risk if a developer forgets a migration). `exportSchema=true` is currently non-functional (no `schemas/` output configured), so there is no machine-checkable schema history to validate future migrations against (`ARCHITECTURE_AUDIT.md` §4).
- **Data integrity:** Every "join" is assembled in-memory from full-table reads (`ARCHITECTURE_AUDIT.md` §4.2) — correctness is fine at small scale, but there is no SQL-level referential query to fall back on if data volume grows. The two-payroll-path problem (§11) is itself a data-integrity risk: a user could reasonably believe the detailed calculator's numbers are "the payroll," when they are not what gets saved.
- **Usability:** Generally good — consistent loading/empty/error patterns across almost every screen (Dashboard's silent-zero-state on error is the one inconsistency found, `UI_UX_FLOW_AUDIT.md` §4). Indonesian-language UI throughout, consistent Material3 styling.
- **Accessibility:** Icon `contentDescription`s are consistently populated; dark theme and dynamic color are supported. Not independently measured at runtime.
- **Performance:** Not measured. In-memory full-table joins are a latency risk only once employee/record counts grow substantially — no evidence either way at current (empty/test) data volumes.
- **Security:** No authentication, no per-field encryption, local SQLite only, no network surface at all (no attack surface beyond physical device access). `OPEN QUESTION` whether that's acceptable for the intended deployment.

## 10. Data model summary

See `ARCHITECTURE_AUDIT.md` §4 for the full entity/DAO inventory. Headline facts: 21 Room entities across employee/org/attendance/leave/overtime/payroll domains, one non-Room config store (`PayrollSetupConfig` via SharedPreferences, added this session, intentionally outside Room per the project's DAL-immutability rule), zero join queries (all joins are in-memory), one migration (`MIGRATION_1_2`, additive-only).

## 11. Business rules (summary — full detail in `BUSINESS_RULES_AUDIT.md`)

The six payroll components (GP/TJ/UM/PH/UL/PL) and their Manager/Non-Manager, tiered-overtime, and PL-threshold rules are implemented **correctly** in `domain/payroll/PayrollCalculator.kt`, exercised only by the non-persisting `PayrollCalculatorScreen`. The pipeline that actually gets saved and finalized (`PreviewPayrollUseCase` → `PayrollResultEntity`) uses an entirely different, simpler formula with no Manager distinction, no proration, no tiering, and no PL — it predates (architecturally) the correct calculator and was never reconciled with it. This is the single biggest gap in the product today.

## 12. Integration/dependency summary

Fully offline, single-module Android app. No backend, no network calls, no third-party services, no analytics. Dependencies are standard AndroidX/Compose/Room/Navigation libraries, no DI framework (hand-rolled `AppContainer`). Full version list in `ARCHITECTURE_AUDIT.md` §1.

## 13. Limitations and assumptions

- This audit read source code and ran two Gradle tasks (`assembleDebug`, `testDebugUnitTest`); it did **not** launch the app on a device/emulator, so no UI claim in this document is "observed at runtime" — all are "confirmed from code."
- `./gradlew assembleDebug` → **BUILD SUCCESSFUL**, APK produced at `app/build/outputs/apk/debug/app-debug.apk` (`CONFIRMED BY BUILD/TEST`, 12-10-2026).
- `./gradlew testDebugUnitTest` → **BUILD SUCCESSFUL**, but the only test executed is the unmodified template (`assertEquals(4, 2+2)`) — this proves the test *harness* works, not that any business rule is correct (`CONFIRMED BY BUILD/TEST`).
- No instrumented/UI test was run (would require an emulator/device, none was available/attempted — `NOT RUN`).
- The project is not under version control (`git status` fails — "not a git repository"), so there is no commit history to cross-reference against; all "as-built" claims come solely from the current state of the files on disk at audit time.

## 14. Open questions

- Is a Manager/Non-Manager (or any job-level/grade) attribute intended to be added to `EmployeeEntity`/`PositionEntity`, and if so, who decides this — HR data entry, or derived from `PositionEntity`?
- Is "Payroll Setup" intended to stay a single global default, or does the business need period-effective-dated or per-department/per-position rates?
- Should the detailed GP/TJ/UM/PH/UL/PL calculator become the actual persisted payroll formula (replacing `PreviewPayrollUseCase`), or should `PreviewPayrollUseCase` be rewritten to call `PayrollCalculator` internally? This is a product decision with real migration-risk implications for `PayrollResultEntity`'s schema (see `GAP_ANALYSIS.md` ID-01).
- Is Kasbon (employee loan/advance) actually required, or was it listed only as an example of "other deductions"? If required, it needs a new entity — which this audit is not authorized to create.
- Is a login/role system in scope at all, or is single-device/single-user access acceptable indefinitely?
- Is the Document feature expected to eventually store real files (SAF/file picker), or is metadata-only tracking (with the current honest on-screen disclosure) the intended final behavior?

## 15. Acceptance criteria for next work (high-level — detailed, numbered criteria live in `GAP_ANALYSIS.md`)

1. A single, unambiguous, user-confirmed decision on which payroll formula becomes "the" persisted payroll calculation, with a concrete plan for reconciling `PayrollResultEntity`'s schema against GP/TJ/UM/PH/UL/PL (this will very likely require a Room migration — out of this audit's read-only scope and blocked by the stated DAL-immutability constraint until the user explicitly lifts it for that specific migration).
2. A persisted Manager/Non-Manager (or equivalent) attribute on the employee/position model, with a migration plan.
3. At least baseline unit tests for `PayrollCalculator` (or whatever formula is chosen as canonical) covering every rule in `BUSINESS_RULES_AUDIT.md`'s rule table, including the boundary cases explicitly called out (exactly 2h overtime, attendanceDays at the 0 and scheduledWorkDays extremes, deductions exceeding earnings).
4. An explicit decision on Kasbon/penalty-as-deduction: either confirmed out of scope, or specified with its own entity design (new, not a repurposed existing one) before implementation starts.
