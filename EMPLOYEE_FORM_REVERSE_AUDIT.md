# EMPLOYEE_FORM_REVERSE_AUDIT.md — MyPayroll

- **Audit date:** 13-10-2026. **Read-only.** No source/config file was created, edited, or deleted to produce this document (it is the only file this audit wrote).
- **Purpose:** Reverse-engineer the actual, current implementation of the Employee/Karyawan form and everything reachable from it, as input to a redesign discussion. This is not a design or implementation proposal.
- **Confidence labeling:** `CONFIRMED FROM CODE` (file read directly, cited by path and line), `INFERRED` (reasonable deduction from code, not directly observed), `NOT VERIFIED` (could not be checked this session), `UNKNOWN / NEEDS CONFIRMATION`. No `OBSERVED AT RUNTIME` claim appears anywhere — **the app was not run on an emulator or device during this audit.**
- **Scoping note:** Five of the nine Employee-detail tabs (Absensi, Cuti, Lembur, Disiplin, and part of Kompensasi) are full screens *shared* with their own top-level navigation destinations outside the Employee module (`Routes.ATTENDANCE`, `Routes.LEAVE`, `Routes.OVERTIME`; Discipline and Compensation have no standalone route but are the same composables). This audit traces how each is scoped/parameterized when embedded under Employee, and their data model as it relates to `EmployeeEntity`, but does not re-derive an exhaustive field-by-field inventory of every dialog inside those modules — that would duplicate a separate, module-level audit. Where a field list is given for these, it is the *entity* (the actual persisted shape), which is complete and authoritative regardless of which screen writes it.

---

## 1. Objective and scope (restated)

Audit, from actual source code only, the Employee list/search screen, Add-Employee dialog, Edit-Employee screen, Employee-detail hub, and all nine tabs reachable from it — tracing each through UI → ViewModel → UseCase → Domain → Repository → DAO → Entity → table, documenting every field's type/validation/default/destination, auditing the `EmployeeEntity` data model and its relationships to other tables, describing the user workflows as confirmed by source (never claiming runtime verification), and reporting evidence-based findings (dead code, no-op buttons, fields displayed-but-unsaved or saved-but-unread, duplication, disconnected validation).

---

## 2. Architecture trace, per screen

**CONFIRMED FROM CODE.** Layering is consistent with the rest of the app (`data.local.entity → data.local.dao → domain.repository → data.repository → domain.usecase → presentation.<feature> → presentation.navigation`); no layer is structurally skipped, but two Employee-specific call sites bypass the use-case layer entirely (noted inline).

| Screen / entry point | File | ViewModel | UseCase(s) called | Repository | DAO | Entity / table |
|---|---|---|---|---|---|---|
| Employee list | `presentation/employee/EmployeeScreen.kt` | `EmployeeViewModel.kt` | `ObserveEmployeesUseCase`, `SaveEmployeeUseCase` (Add dialog only), `DeleteEmployeeUseCase` (unreachable, §6) | `EmployeeRepository` → `EmployeeRepositoryImpl` | `EmployeeDao` | `employee` |
| Add Employee | `EmployeeScreen.kt` → `AddEmployeeDialog` (same file, lines 102–146) | `EmployeeViewModel.save()` | `SaveEmployeeUseCase` (`employee.id==0` branch → `repository.insert`) | `EmployeeRepository` | `EmployeeDao.insert` | `employee` |
| Edit Employee | `presentation/employee/EmployeeEditScreen.kt` | `EmployeeEditViewModel.kt` | **None** — `EmployeeEditViewModel.save()` (line 37) calls `repository.update(updated)` directly, bypassing `SaveEmployeeUseCase` entirely. `init` (line 29) also calls `repository.getById()` directly, not through a use case. | `EmployeeRepository` | `EmployeeDao.update` | `employee` |
| Employee detail hub | `presentation/employee/EmployeeDetailScreen.kt` | none dedicated — built directly from `ObserveEmployeeProfileUseCase` | `ObserveEmployeeProfileUseCase` (`domain/usecase/ObserveEmployeeProfileUseCase.kt`) | `EmployeeRepository`, `EmployeePositionHistoryRepository`, `EmployeeContractRepository`, `EmployeeSalaryHistoryRepository`, `DepartmentRepository`, `PositionRepository` (6 repos combined in-memory, no join query exists at the DAO level — line 21-24 of the use case says so explicitly) | 6 DAOs | `employee`, `employee_position_history`, `employee_contract`, `employee_salary_history`, `department`, `position` |

Tab-by-tab trace continues in §3; each tab's own ViewModel/UseCase/Repository/DAO chain is given there since several are shared, employeeId-parameterized versions of other modules' screens.

---

## 3. Tab-by-tab inventory

### 3.0 Employee List (`EmployeeScreen.kt`)

1. Not a tab — the entry screen. 2. `presentation/employee/EmployeeScreen.kt`. 3. Fields, in display order: search field (`SearchField`, placeholder "Cari nama atau NIK"), error banner (conditional), "Total: N karyawan" label, then a card per employee showing `name`, `"NIK: ${nik}"`, and a `StatusChip` for `status`. 4. Search = free text, client-side substring match on `name`/`nik`, case-insensitive (`EmployeeViewModel.kt:29-31`). 5. N/A (not a form). 6. N/A. 7. N/A. 8. Source: `ObserveEmployeesUseCase()` → `EmployeeDao.observeAll()` (`ORDER BY name`, `EmployeeDao.kt:13`); list is **re-sorted by name again** client-side in the ViewModel (`EmployeeViewModel.kt:32`) — redundant since the DAO query already orders by name, but harmless. No sort/filter-by-status control exists in the UI despite `EmployeeStatus` having 3 values. 9. N/A — list only. 10. **FAB "+"** opens `AddEmployeeDialog`; tapping a `Card` calls `onOpenEmployee(employee.id)` → navigates to detail. **No delete affordance anywhere in this screen** despite `EmployeeViewModel.delete()` existing and being fully wired to `DeleteEmployeeUseCase` → `EmployeeDao.delete` (`EmployeeViewModel.kt:46-51`) — see Finding F-1. 11. `ObserveEmployeesUseCase`/`SaveEmployeeUseCase`/`DeleteEmployeeUseCase`, `EmployeeRepository`/`Impl`, `EmployeeDao`, `employee` table. 12. None. 13. Delete is implemented end-to-end but has no UI trigger (F-1); no status filter control (minor).

**Add Employee dialog** (same file, `AddEmployeeDialog`, lines 102–146):

| # | Field | Type | Required? | Default | Validation | Notes |
|---|---|---|---|---|---|---|
| 1 | NIK | free text | Yes (button-gate only) | `""` | `nik.isNotBlank()` gates the Simpan button; **no client-side duplicate check** even though `EmployeeRepository.getByNik()` exists (`EmployeeRepository.kt:10`) — it is never called from this dialog or its ViewModel. The only real duplicate protection is the DB's `unique` index on `nik` (`EmployeeEntity.kt:7`) plus `OnConflictStrategy.ABORT` (`EmployeeDao.kt:19`), which throws a raw `SQLiteConstraintException` on save, surfaced to the user via `e.message` (`EmployeeViewModel.kt:42`) — **not a friendly "NIK sudah dipakai" message.** | trimmed on save (`.trim()`) |
| 2 | Nama | free text | Yes | `""` | `name.isNotBlank()` | trimmed |
| 3 | Telepon | free text | No | `""` | none | stored as `null` if blank (`.ifBlank { null }`) |
| 4 | Email | free text | No | `""` | **no email-format validation at all** | stored as `null` if blank |
| 5 | Alamat | free text | No | `""` | none | stored as `null` if blank |
| 6 | Tanggal masuk | `DateField` → `LocalDate?` | Yes | today (`LocalDate.now()`) | `joinDate != null` gates button | |
| 7 | Status | `EnumDropdownField<EmployeeStatus>` | Yes (always has a value) | `ACTIVE` | enum-constrained by construction | |

Birth date and gender are **not collected on Add** — only on Edit (§3.0 Edit, below). A newly-added employee's `birthDate`/`gender` are `null` by the `EmployeeEntity` defaults until someone later opens Edit.

### 3.0b Edit Employee (`EmployeeEditScreen.kt`)

Same 7 Add fields **plus**: Tanggal lahir (`DateField`, clearable, default = existing value or `null`), Jenis kelamin (`EnumDropdownField<Gender>`, default `employee.gender ?: Gender.MALE` — **note:** if `gender` was `null`, the dropdown displays "MALE" as if selected, but the underlying `gender` state variable is still typed to show whatever the user doesn't change; if the user never touches the dropdown and saves, `gender` is written as `MALE` even though it was never explicitly chosen — see Finding F-2), Tanggal keluar (`resignDate`, clearable). Save button text toggles "Menyimpan..."/"Simpan" based on `isSaving`; `LaunchedEffect(state.saved)` (line 44) navigates back (`onDone()`) automatically once `saved` flips true — no explicit "saved!" confirmation message is shown to the user before the screen closes (the whole screen disappears on success instead).

Save path: `EmployeeEditViewModel.save()` → `repository.update()` directly (bypasses `SaveEmployeeUseCase`, confirmed above). Failure path: `errorMessage` set and shown via `ErrorBanner`; screen stays open, `saved` stays false.

### 3.1 "Ringkasan" (`ProfileSummaryTab`, inside `EmployeeDetailScreen.kt:100-143`)

Read-only summary, not a form. 4 cards, in order: (1) name + status chip + NIK + phone/email/address (each conditionally shown only if non-null) + join date + resign date (conditional); (2) "Jabatan Saat Ini" — `currentPosition?.name ?: "Belum ditentukan"`, `currentDepartment?.name ?: "-"`; (3) "Kontrak Aktif" — contract number + start/end date, or "Tidak ada kontrak aktif"; (4) "Gaji Pokok Terakhir" — `latestSalary?.basicSalary?.toRupiah() ?: "Belum ada data gaji"`. All four cards' data comes from the single `EmployeeProfile` built by `ObserveEmployeeProfileUseCase` (§2). No edit affordance on this tab at all — editing requires the top-bar pencil icon (`onEdit`), which only edits the core `EmployeeEntity` fields (§3.0b), not position/contract/salary (those are edited from their own tabs).

`currentPosition`/`currentDepartment` resolution logic (`ObserveEmployeeProfileUseCase.kt:44-48`): picks the `EmployeePositionHistoryEntity` row with `endDate == null` (i.e., "currently open"), falling back to the most recent by `startDate` if none is open. Same pattern for `activeContract` (`ContractStatus.ACTIVE`, falling back to most recent by `startDate`).

### 3.2 "Posisi" (`PositionHistoryScreen(employeeId)`, `presentation/organization/PositionHistoryScreen.kt`)

| # | Field (Add dialog) | Type | Required | Default | Validation |
|---|---|---|---|---|---|
| 1 | Posisi | dropdown of `PositionEntity` | Yes | first in list | `position != null` gates button; **if no positions exist yet, the dialog shows only a text hint and the picker doesn't render at all** (line 106-111) |
| 2 | Departemen | dropdown of `DepartmentEntity` | Yes | first in list | same pattern |
| 3 | Berlaku sejak | `DateField` | Yes | today | `startDate != null` |
| 4 | Alasan (promosi/mutasi) | free text | No | `""` | none; stored `null` if blank |

List rows show `"${positionName} - ${departmentName}"`, date range, reason. **Save behavior is a "close-and-open" transfer, not a plain insert**: `EmployeePositionHistoryViewModel.transfer()` (`EmployeePositionHistoryViewModel.kt:53-68`) finds the currently-open row (`endDate == null`) and closes it (`endDate = startDate.minusDays(1)`) *only if* its `startDate` is before the new one, then inserts a new open-ended row. `supervisorId` is a real, FK-enforced column on `EmployeePositionHistoryEntity` (self-referencing to `employee`, `EmployeePositionHistoryEntity.kt:14`) but **this dialog has no UI control for it at all — `transfer()` is always called with `supervisorId = null`** (`PositionHistoryScreen.kt:83`). Finding F-3.

Entity/table: `EmployeePositionHistoryEntity` / `employee_position_history` — `id, employeeId, positionId, departmentId, supervisorId(unused), startDate, endDate, reason, notes(never set by any UI — no dialog field for it)`.

### 3.3 "Kontrak" (`ContractMonitoringTab(employeeId)`, inside `presentation/organization/OrganizationScreen.kt:122-184+`)

This exact composable, with `employeeId = null`, is also the content of the top-level "Organisasi & Kontrak" nav destination's contract sub-tab — **the Employee-tab and the global contract-monitoring screen are the literal same code**, just filtered. List rows: employee name (redundant inside the per-employee tab, since it's always the same name — only useful in the unscoped/global view), contract number + type name, status chip, an expiry warning chip (`daysToExpiry in 0..30` → "Berakhir N hari lagi" warning; `< 0` → "Sudah lewat tanggal akhir" danger) computed client-side (`EmployeeContractViewModel.kt:62`, `ChronoUnit.DAYS.between(today, endDate)` — **not reactive to day changes without a recomposition trigger**, i.e., if the app is left open across midnight the badge won't update until some other state change forces recomposition — `NOT VERIFIED` at runtime). "Akhiri Kontrak" button appears only on `ACTIVE` contracts.

Add-contract fields (dialog defined further in `OrganizationScreen.kt`, not fully re-read in this pass — signature confirmed via `EmployeeContractViewModel.addContract(employeeIdForNew, contractNumber, contractTypeId, startDate, endDate)`): employee (only on the global/unscoped view — pre-filled when inside the Employee tab), contract number (free text — **must be globally unique**, `Index(value=["contractNumber"], unique=true)`, `EmployeeContractEntity.kt:15`, with the same raw-DB-error UX risk as NIK, F-? see F-4), contract type (dropdown), start date, end date (nullable). **`documentId` is a real FK column on `EmployeeContractEntity` (`SET_NULL` on delete, `EmployeeContractEntity.kt:13`) but `addContract()`'s parameter list has no `documentId` — it can never be set through this UI path**, always `null` on insert (Finding F-5, same class of issue as F-3's `supervisorId`). Terminate flow (`terminate()`, `EmployeeContractViewModel.kt:84-92`) sets `status=TERMINATED`, `terminationDate=today`, `terminationReason` — reason is collected via a separate confirm dialog (`terminateTarget`, not fully re-read this pass).

Entity/table: `EmployeeContractEntity` / `employee_contract` — `id, employeeId, contractNumber, contractTypeId, startDate, endDate, documentId(unused via UI), status, terminationDate, terminationReason, notes(never set by any UI)`.

### 3.4 "Kompensasi" (`EmployeeCompensationScreen(employeeId, showTopBar=false)`, `presentation/compensation/`)

**State as of this session** (the "Komponen" sub-tab and its backing `PayComponentEntity`/`EmployeePayComponentHistoryEntity`/DAOs were deleted entirely earlier this session, Room migrated v2→v3 — `CONFIRMED FROM CODE`, re-read fresh for this audit, not assumed from memory). Two sub-tabs remain:

**"Gaji"** — list of `EmployeeSalaryHistoryEntity` sorted by `effectiveDate` desc, each row showing `basicSalary.toRupiah()`, `"Berlaku sejak {date} - {salaryType}"`, `reason` (conditional). Add dialog fields: Berlaku sejak (`DateField`, default today), Gaji pokok Rp (digit-only text field, no min/max/zero-check — `amount.toLong()` is called unguarded in the button `onClick`, so **an empty string would throw `NumberFormatException` at click time**, though the button is gated `amount.isNotBlank()` so in practice this can't be reached with a fully-empty string; a string that overflows `Long` could still throw — `NOT VERIFIED`, no test covers this), Tipe (`EnumDropdownField<SalaryType>`, default `MONTHLY`), Alasan (free text, optional). `referenceDocumentId`/`approvedBy`/`notes` columns exist on the entity but **no UI field sets any of them** — always `null` on insert (Finding F-6).

**"Insentif"** — list of `EmployeeIncentiveEntity` sorted by `date` desc, each row showing `date.toDisplay()`, `amount.toRupiah()`, `reason` (conditional). **No longer shows a component name** (the join target, `PayComponentEntity`, no longer exists). Add dialog fields: Tanggal (default today), Jumlah Rp (digit-only), Alasan (optional). `payComponentId` is hard-coded to `0L` on every insert (`EmployeeCompensationViewModel.kt:65`) — it is a vestigial, non-FK column now (its foreign key to the deleted `pay_component` table was severed in `MIGRATION_2_3`, confirmed from this session's own earlier work). `workOrderId`, `referenceId`, `approvedBy`, `notes` columns exist but are never set by any UI (Finding F-7).

Entities/tables: `EmployeeSalaryHistoryEntity`/`employee_salary_history`, `EmployeeIncentiveEntity`/`employee_incentive`.

**Cross-module dependency, confirmed:** `EmployeeSalaryHistoryEntity` (via `latestSalary`) feeds both the "Ringkasan" tab (§3.1) and `PreviewPayrollUseCase.basicSalary` (`PreviewPayrollUseCase.kt`, `allSalaryHistory.filter{...}.maxByOrNull{effectiveDate}`). `EmployeeIncentiveEntity` feeds `PreviewPayrollUseCase.totalIncentive` the same way. **Neither of these was affected by this session's PayComponent deletion** — only `totalAllowance` was (see §3.8 below, the single most significant finding of this audit).

### 3.5 "Absensi" (`AttendanceScreen(employeeId)`, `presentation/attendance/AttendanceScreen.kt`)

Shared with the top-level `Routes.ATTENDANCE` destination (called there with `employeeId=null, showTopBar=true`; inside Employee detail, `showTopBar` defaults to `employeeId == null` → `false`, confirmed at `AttendanceScreen.kt:62`). Internally has its own 2-tab `TabRow`: "Absensi" (batch entry/edit, backed by `AttendanceViewModel`) and "Pengecualian" (`AttendanceExceptionViewModel`) — **both sub-tabs render regardless of whether `employeeId` is null or set** (no conditional hiding like Leave's "Jenis Cuti", confirmed by reading lines 60-80; the "Pengecualian" tab is not gated on `employeeId == null`, unlike `LeaveScreen`'s equivalent — `NOT VERIFIED` beyond this file, but the code shows both ViewModels constructed unconditionally and both `Tab`s always shown). Entity/table: `AttendanceEntity`/`attendance` (unique index on `employeeId`+`workDate`), `AttendanceExceptionEntity`/`attendance_exception`.

**Cross-module dependency, confirmed:** `AttendanceEntity.lateMinutes` feeds `PreviewPayrollUseCase.attendanceDeduction` (`lateMinutes * policy.lateDeductionPerMinute`) and, as of this session's new (not-yet-wired) "Penalty Keterlambatan Bertingkat" feature, is the intended real data source for `domain/payroll/LatePenaltyCalculator` — that calculator exists and is unit-tested but **is not called from anywhere in the Employee or Payroll pipeline yet** (confirmed — it has no call sites outside its own test file).

### 3.6 "Cuti" (`LeaveScreen(employeeId)`, `presentation/leave/LeaveScreen.kt`)

Shared with `Routes.LEAVE`. Internal "Jenis Cuti" (leave-type master-data) sub-tab is correctly hidden when `employeeId != null` (`LeaveScreen.kt:60`, `if (employeeId == null) Tab(...)`) — this is the one place among the 4 reused modules that *does* conditionally hide its global-admin sub-tab inside the Employee context; Attendance (§3.5) does not. Entity/table: `LeaveRequestEntity`/`leave_request` (FK to `employee`, `leave_type`; loose `documentId: Long?` with **no FK constraint** to `document` — inconsistent with `EmployeeContractEntity.documentId`, which *is* FK-constrained — Finding F-8), `LeaveTypeEntity`/`leave_type`.

### 3.7 "Lembur" (`OvertimeScreen(employeeId, showTopBar=false)`, `presentation/overtime/`)

Shared with `Routes.OVERTIME` (`employeeId=null, showTopBar=true` there). Entities: `OvertimeAssignmentEntity`/`overtime_assignment`, `OvertimeRecordEntity`/`overtime_record`. Not re-traced field-by-field in this pass (see scoping note) — confirmed only that `OvertimeRecordEntity.calculationAmount` is the value that reaches `PreviewPayrollUseCase.totalOvertimeAmount` via `OvertimeRecordRepository`, filtered to `status == APPROVED` and the assignment's `workDate` falling inside the payroll period (`PreviewPayrollUseCase.kt`, lines ~60-67).

### 3.8 "Disiplin" (`DisciplineScreen(employeeId, showTopBar=false)`, `presentation/discipline/`)

Shared, same `employeeId?`/`showTopBar` pattern as Overtime. Entity: `EmployeeDisciplinaryActionEntity`/`employee_disciplinary_action` — `id, employeeId, incidentDate, actionDate, actionType(enum VERBAL_WARNING..SP3,OTHER), category, description, referenceDocumentId(no FK), issuedBy, validUntil, notes`. **Confirmed, carried forward from this project's own prior audit (`GAP_ANALYSIS.md` GAP-05) and re-verified fresh here: this entity has no monetary field, and `PreviewPayrollUseCase` never reads `EmployeeDisciplinaryActionRepository` at all** — disciplinary actions recorded on this tab have **zero effect on any payroll calculation**, confirmed still true after this session's changes (the repository import doesn't even appear in `PreviewPayrollUseCase.kt`).

### 3.9 "Payroll" (`EmployeePayrollHistoryTab`, inside `EmployeeDetailScreen.kt:145-172`)

Read-only list of this employee's `PayrollResultEntity` rows (`container.payrollResultRepository.observeByEmployee(employeeId)`), each showing period name (joined in-memory against `payrollPeriodRepository.observeAll()`), a status chip, `grossPay.toRupiah()`, `netPay.toRupiah()`. No edit/create affordance — purely a read view of whatever `ApprovePayrollUseCase`/`FinalizePayrollUseCase` already persisted.

**★ Single most significant finding of this audit (F-9):** `ApprovePayrollUseCase.kt:38` writes `totalAllowance = line.totalAllowance` straight from `PayrollLineItem` into the persisted `PayrollResultEntity`, and `PayrollLineItem.grossPay`/`.netPay` are computed properties that *include* `totalAllowance` in their sum (`PayrollLineItem.kt`: `grossPay = basicSalary + totalAllowance + totalIncentive + totalOvertimeAmount`). Since this session's deletion of `PayComponentEntity`/`EmployeePayComponentHistoryEntity` forced `PreviewPayrollUseCase.totalAllowance` to a hardcoded `0L` (its only data source was removed, confirmed in `PreviewPayrollUseCase.kt`), **every payroll approved or finalized from now on will show a `grossPay`/`netPay` on this exact "Payroll" tab that is permanently lower than it would have been before this session**, with no UI indication that an allowance component is missing. A user comparing an old payroll result (approved before this session) against a new one on this same tab, for the same employee, would see an unexplained drop. This was flagged in this session's own `mod-131026-2.md` but is restated here because it is directly visible on an Employee-form tab, which is squarely this audit's subject.

---

## 4. Data model and relationship audit

**`EmployeeEntity`** (`data/local/entity/EmployeeEntity.kt`) — table `employee`:

| Field | Type | Nullable | Default | Constraint |
|---|---|---|---|---|
| id | Long | No | `0` (autoGenerate) | PK |
| nik | String | No | — | unique index |
| name | String | No | — | non-unique index (used for `ORDER BY`) |
| birthDate | LocalDate | Yes | `null` | — |
| gender | Gender (enum) | Yes | `null` | — |
| phone | String | Yes | `null` | — |
| email | String | Yes | `null` | — |
| address | String | Yes | `null` | — |
| joinDate | LocalDate | No | — | — |
| resignDate | LocalDate | Yes | `null` | — |
| status | EmployeeStatus (enum: ACTIVE/RESIGNED/INACTIVE) | No | `ACTIVE` | — |

**No Manager/Non-Manager classification field exists anywhere on this entity or any related one** — `domain/payroll/EmployeePayrollType` (MANAGER/NON_MANAGER) is a UI-only dropdown value in the separate, non-persisting Payroll Calculator, with no link back to `EmployeeEntity`. (This matches `GAP_ANALYSIS.md` GAP-02 from this project's own prior audit; re-confirmed here specifically in the context of the Employee form, since no tab anywhere in the 9-tab detail screen offers to set or display this classification.)

**Entities with a direct FK to `employee.id`** (`CONFIRMED FROM CODE`, by `grep` across `data/local/entity/*.kt` and manual read of each): `attendance`, `attendance_exception`, `employee_contract`, `employee_disciplinary_action`, `employee_incentive` *(only until this session; FK now severed — see §3.4)*, `employee_position_history` (twice: `employeeId` and self-referencing `supervisorId`), `employee_salary_history`, `leave_request`, `overtime_assignment`, `payroll_result`. All of these are 1-to-many from Employee's side (one employee, many rows), confirmed by the absence of any unique constraint on `employeeId` alone in any of them except `attendance` (unique on `employeeId`+`workDate` together, i.e., still many-per-employee, just one-per-day).

**Entities with NO relationship to `employee` at all, despite being conceptually employee-adjacent:** `document` (`DocumentEntity` — confirmed zero `employeeId`/FK field; the "Lainnya → Dokumen" menu item is labeled "Metadata dokumen karyawan" in `AppNavGraph.kt:153`, which is misleading — the document list is global, not filterable or attributable to any specific employee at the entity level). Other entities *reference* a document by a loose `documentId`/`referenceDocumentId` Long (`employee_contract.documentId` — **FK-enforced**; `leave_request.documentId`, `employee_salary_history.referenceDocumentId`, `employee_disciplinary_action.referenceDocumentId`, `attendance_exception.documentId` — **none FK-enforced**, inconsistent with the contract one, Finding F-8/F-10).

**Duplicated/derived data across entities, confirmed:**
- "Current position/department" is *derived* (computed at read time by picking the open-ended `employee_position_history` row) — not stored redundantly on `EmployeeEntity` itself. Good: no duplication risk here.
- "Current basic salary" is likewise derived from `employee_salary_history` (latest `effectiveDate`), not duplicated on `EmployeeEntity`.
- `employee_contract.employeeId` + `employee_position_history.employeeId` + `employee_salary_history.employeeId` are all independent FKs to the same parent with no cross-validation between them (e.g., nothing enforces that a contract's date range overlaps a position-history row, or that a salary change date aligns with a contract) — `NOT VERIFIED` whether this is ever a real-world problem, but no constraint exists either way.

**Nullability/default pattern:** every "free text, optional" field across every entity touched by the Employee tabs follows the same `?.ifBlank { null }` convention at the ViewModel layer before insert — consistent, `CONFIRMED FROM CODE` across `EmployeeScreen.kt`, `EmployeeEditScreen.kt`, `PositionHistoryScreen.kt`, `EmployeeCompensationViewModel.kt`.

**Other modules reading Employee data**, confirmed by this audit: `domain/usecase/PreviewPayrollUseCase.kt` (reads `employeeRepository.observeAll()`, filters `ACTIVE`), `presentation/payroll/PayrollCalculatorViewModel.kt` (employee picker for the ad-hoc calculator), `presentation/payrollsetup/` (new this session) — **confirmed it does NOT read or write any Employee-specific entity**; it is purely global config (SharedPreferences-backed `PayrollCalendarRepository`/`OvertimeRuleConfigRepository`/`LatePenaltyConfigRepository`), so the earlier concern in this audit's brief about conflating the two is resolved: there is no overlap.

---

## 5. Workflow and UI behavior audit

All of the following is **"confirmed by source"** unless marked otherwise. **No runtime/emulator testing was performed this session** — every behavioral claim below is a trace of the code path, not an observation of the running app.

- **Open list → search → select:** confirmed. `EmployeeScreen` loads via `ObserveEmployeesUseCase` (reactive `Flow`), search filters client-side, tapping a card navigates via `onOpenEmployee(id)` (wired in `AppNavGraph.kt`, not re-read this pass but consistent with every other list→detail pattern in the app).
- **Create:** confirmed. FAB → `AddEmployeeDialog` → `viewModel.save()` → `SaveEmployeeUseCase` → insert. Failure (e.g. duplicate NIK) surfaces as a raw exception message, not a friendly validation message (F-?, see §3.0).
- **Navigate through all tabs:** confirmed structurally — all 9 tabs are always rendered in the same `ScrollableTabRow`, none are conditionally hidden based on employee state (e.g., a `RESIGNED` employee still shows all 9 tabs, including ones like "Lembur"/"Cuti" that may not make sense post-resignation — `NOT VERIFIED` whether this is intentional).
- **Edit and save:** confirmed for the core `EmployeeEntity` fields via the pencil icon → `EmployeeEditScreen`. Editing data on any *other* tab (position, contract, salary, incentive) is a separate add-only flow on that tab, not an "edit" in the traditional sense — e.g., there is no way to edit or delete a past salary-history row or incentive row once saved; only new rows can be added (`CONFIRMED`: no `update`/`delete` call site exists in `EmployeeCompensationViewModel.kt`, `EmployeePositionHistoryViewModel.kt` for salary/position rows — position history's `update()` call is only ever used internally by `transfer()` to close the previous row, never exposed as a user-facing edit).
- **Cancel / leave with unsaved changes:** `NOT VERIFIED` and likely absent. `EmployeeEditScreen`'s `nik`/`name`/etc. are plain `remember(employee.id) { mutableStateOf(...) }` with no dirty-check; the top-bar back arrow (`onDone`) navigates away immediately with no confirmation dialog, discarding any typed-but-unsaved edits silently. Same pattern in every Add dialog in this module (dismissing the dialog discards the draft with no confirmation) — this is consistent across the whole app's dialogs, not unique to Employee, but confirmed present here too.
- **Delete an employee:** **not supported from the UI**, despite being fully implemented in the ViewModel/UseCase/DAO chain (F-1). `EmployeeStatus` (RESIGNED/INACTIVE) is the only user-facing way to mark an employee as no longer active; no cascade-delete UI path exists (nor would it be safe to add one without reviewing the 10 FK relationships in §4, several of which are `CASCADE` and would silently wipe attendance/contract/salary/position history).
- **Validation errors / DB failures:** every screen in this module uses the same `errorMessage: String?` + `ErrorBanner` pattern, fed by `runCatching { ... }.onFailure { e -> _errorMessage.value = e.message ?: "..." }` — confirmed consistent across `EmployeeViewModel`, `EmployeeEditViewModel`, `EmployeePositionHistoryViewModel`, `EmployeeContractViewModel`, `EmployeeCompensationViewModel`. The message shown to the user is whatever `Exception.message` happens to be for a Room/SQLite constraint violation — readable to a developer, not necessarily to an HR user (e.g., a raw "UNIQUE constraint failed: employee.nik (code 1555)" style string is plausible for the NIK-duplicate case — `NOT VERIFIED` exact wording without running it, since that string comes from the Android SQLite driver, not this app's own code).
- **Navigate to other modules from Employee data:** confirmed one-directional only — Employee detail embeds Attendance/Leave/Overtime/Discipline/Compensation *content*, but none of those tabs offer a "open in its own full screen" shortcut back out; conversely, the top-level Attendance/Leave/Overtime nav destinations show *all* employees' data (not just one), with no "jump to this employee's profile" link from a row. `NOT VERIFIED` beyond what's described in §3's per-tab notes.

---

## 6. Findings

Evidence bar: every finding below cites a specific file (and line where practical). None are speculative.

| ID | Finding | Evidence | Severity (evidence-based, not a recommendation) |
|---|---|---|---|
| F-1 | Employee delete is fully implemented (ViewModel → UseCase → Repository → DAO) but has no UI trigger anywhere in the list or detail screens | `EmployeeViewModel.kt:46-51` (`delete()`), zero call sites of `viewModel.delete` or `viewModel::delete` in `EmployeeScreen.kt`/`EmployeeDetailScreen.kt` | Dead-but-reachable code path; no functional bug since it's simply unused |
| F-2 | Edit screen's gender dropdown defaults to `MALE` display when the underlying value is `null`, with no "unset" option — saving without touching the dropdown writes `MALE`, not `null` | `EmployeeEditScreen.kt:66,82`: `var gender by remember(...) { mutableStateOf(employee.gender) }` then `EnumDropdownField("Jenis kelamin", gender ?: Gender.MALE, ...)` — the displayed/selectable value is coerced, but note `gender` itself (not `gender ?: Gender.MALE`) is what's passed to `employee.copy(gender = gender, ...)` on save (line 94) — so if the user never opens the dropdown, the saved value stays whatever it was (`null` stays `null`); the risk is only if the user opens the dropdown (which always shows MALE pre-selected for a null value) and confirms it without intending to set a value | Minor UX risk: a `null` gender can be accidentally set to MALE by a user who merely opens and confirms the dropdown without changing it, because it never visually represents "unset" |
| F-3 | `EmployeePositionHistoryEntity.supervisorId` is a real, FK-enforced column with no UI control anywhere to set it — always inserted as `null` | `EmployeePositionHistoryEntity.kt:14` (FK definition); `PositionHistoryScreen.kt:83` (`viewModel.transfer(positionId, departmentId, null, startDate, reason)`) | Saved-but-never-populated field (schema supports a feature the UI never exposes) |
| F-4 | Contract number must be globally unique (DB-enforced) with no client-side pre-check or friendly duplicate message, same class of issue as NIK (F in §3.0) | `EmployeeContractEntity.kt:15` (`Index(..., unique=true)`) | Same UX risk as the NIK case |
| F-5 | `EmployeeContractEntity.documentId` is FK-enforced but `EmployeeContractViewModel.addContract()` has no parameter for it — always `null` on insert via this UI | `EmployeeContractEntity.kt:13`, `EmployeeContractViewModel.kt:68` | Saved-but-never-populated field |
| F-6 | `EmployeeSalaryHistoryEntity.referenceDocumentId`, `.approvedBy`, `.notes` exist but no Add-Salary UI field sets any of them | `EmployeeSalaryHistoryEntity.kt:19-22`, `EmployeeCompensationViewModel.kt:51-59` (insert call lists only `employeeId, effectiveDate, basicSalary, salaryType, reason`) | Saved-but-never-populated fields |
| F-7 | `EmployeeIncentiveEntity.workOrderId`, `.referenceId`, `.approvedBy`, `.notes`, and now `.payComponentId` (vestigial post-migration) exist but no Add-Incentive UI field sets any of them (payComponentId is hardcoded `0L`) | `EmployeeCompensationViewModel.kt:64-66` | Saved-but-never-populated / vestigial fields |
| F-8 | Document linkage is inconsistent: `employee_contract.documentId` is FK-enforced, but `leave_request.documentId`, `employee_salary_history.referenceDocumentId`, `employee_disciplinary_action.referenceDocumentId`, `attendance_exception.documentId` are plain, unconstrained `Long?` columns | `EmployeeContractEntity.kt:13` vs. the other four entities' field declarations (read directly, no FK annotation present on any of them) | Schema inconsistency — same conceptual relationship (row → supporting document) modeled two different ways across five entities |
| F-9 | **(Restated from §3.9 — the most significant finding.)** `PreviewPayrollUseCase.totalAllowance` is now permanently `0`, and this flows directly into the persisted `PayrollResultEntity.totalAllowance`/`.grossPay`/`.netPay` shown on the Employee-detail "Payroll" tab, with no UI indication that a component is missing | `PreviewPayrollUseCase.kt` (`totalAllowance` line), `ApprovePayrollUseCase.kt:38`, `PayrollLineItem.kt` (`grossPay` computed property), `EmployeeDetailScreen.kt:145-172` (the tab that displays it) | Direct, user-visible regression on an Employee-form tab, as of this session's own earlier PayComponent deletion |
| F-10 | `EmployeeDetailScreen.kt` imports `com.mypayroll.presentation.document.DocumentScreen` (line 44) but never calls it — Documents is not one of the 9 tabs, and `DocumentEntity` has no employee relationship at all, despite the global menu entry's label calling it "dokumen karyawan" | `EmployeeDetailScreen.kt:44` (import), full-file read confirms no call site; `DocumentEntity.kt` (no `employeeId` field); `AppNavGraph.kt:153` (misleading label) | Dead import + mislabeled, unscoped feature — likely an abandoned integration attempt |
| F-11 | `EmployeeEditViewModel` bypasses `SaveEmployeeUseCase` and calls `EmployeeRepository.update()`/`.getById()` directly, while the Add-Employee path on the list screen does go through `SaveEmployeeUseCase` — inconsistent layering for what is otherwise the same entity/operation family | `EmployeeEditViewModel.kt:29,37` vs. `EmployeeViewModel.kt:41` | Architectural inconsistency; no functional difference today only because `SaveEmployeeUseCase` itself currently contains no logic beyond the insert/update branch (`SaveEmployeeUseCase.kt`) — but any future validation added to the use case would silently not apply to Edit |
| F-12 | `EmployeeRepository.getByNik()` exists and is implemented end-to-end but has zero call sites anywhere in the Employee module | `EmployeeRepository.kt:10`, confirmed via grep that only the interface/impl declare it, no ViewModel calls it | Unused, fully-wired capability — a pre-save duplicate-NIK check could use this but doesn't |
| F-13 | List screen re-sorts by name client-side even though the DAO query already returns rows `ORDER BY name` | `EmployeeDao.kt:13` vs. `EmployeeViewModel.kt:32` | Harmless redundancy, not a bug |
| F-14 | No sort or status-filter control in the Employee list UI, despite `EmployeeStatus` having 3 meaningfully different values (ACTIVE/RESIGNED/INACTIVE) that are only visible per-row via the status chip | `EmployeeScreen.kt` (full file read — only a free-text search field exists) | Missing UI capability, not a bug |
| F-15 | Edit screen shows no explicit "saved successfully" confirmation — on success the whole screen is popped via `LaunchedEffect(state.saved) { onDone() }`, so the user's only feedback is the screen disappearing | `EmployeeEditScreen.kt:44` | Minor UX gap relative to other screens in this app that do show an explicit saved-message (e.g. Payroll Setup's screens, this session's own work) |
| F-16 | No dirty-state / unsaved-changes confirmation on leaving Edit Employee or any Add dialog in this module | `EmployeeEditScreen.kt`, `EmployeeScreen.kt` `AddEmployeeDialog` — plain `remember`-backed local state with no comparison against the original on dismiss/back | Consistent with the rest of the app's dialogs, not Employee-specific, but present here too |

---

## 7. What could not be verified, and why

- **No runtime/emulator/device testing was performed.** This project has no Robolectric or instrumented test dependency configured (`CONFIRMED FROM CODE`: `app/build.gradle.kts` test dependencies are `junit`, `androidx.espresso.core`, `androidx.junit`, `androidx.compose.ui.test.junit4` — all under `androidTestImplementation`, meaning they require a connected device/emulator that was not available/used this session). Every "the button does X" / "the screen shows Y" claim above is a static trace of the Kotlin/Compose source, not an observation of the app actually running.
- **Zero automated tests exist for anything in the Employee module** — `find app/src/test -iname "*Employee*"` returns nothing. This audit's findings are therefore entirely a manual code trace with no test suite to cross-check against or lean on.
- **Several Add dialogs inside `OrganizationScreen.kt`** (`AddContractDialog`, the terminate-contract confirm dialog) were traced via their call sites and `ViewModel` function signatures rather than a full line-by-line read of the dialog composables themselves — the *data* they can produce is fully characterized (via the `ViewModel` methods they call), but exact field *ordering*/*labels* in those two specific dialogs were not independently re-verified pixel-for-pixel against the brief's "exact order displayed" requirement.
- **Attendance/Leave/Overtime/Discipline's own internal field-level forms** (e.g., every field of the attendance batch-entry row, every field of a leave request, every field of an overtime assignment/record) were deliberately not re-derived to the same depth as the Employee-specific tabs (Posisi/Kontrak/Kompensasi) — see the scoping note at the top of this document. Their *entities* and *cross-module relevance to Employee/payroll* are fully covered; their own screens' complete UI inventories are not, since those are shared modules with their own top-level navigation identity outside the Employee form.
- **Whether `EmployeeDetailScreen`'s 9 tabs behave sensibly for a `RESIGNED`/`INACTIVE` employee** (e.g., whether "Lembur"/"Cuti" should even be enterable) is unverified — no conditional logic based on `employee.status` was found gating any tab, but whether that's a real problem depends on business intent not captured anywhere in the code.
