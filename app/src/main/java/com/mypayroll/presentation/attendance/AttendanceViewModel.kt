package com.mypayroll.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.core.format.toDisplay
import com.mypayroll.data.local.entity.AttendanceEntity
import com.mypayroll.data.local.entity.AttendanceStatus
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.data.local.entity.EmployeeStatus
import com.mypayroll.domain.repository.AttendanceRepository
import com.mypayroll.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AttendanceRow(val attendance: AttendanceEntity, val employeeName: String)

/**
 * One editable line of the batch entry list. Drafts live in the ViewModel, never in the
 * composition, so neither a recomposition nor another "Load Karyawan" tap loses the edits.
 */
data class AttendanceDraft(
    val employeeId: Long,
    val employeeName: String,
    val nik: String,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val lateMinutes: Int = 0,
    val earlyLeaveMinutes: Int = 0,
    val notes: String? = null,
    val edited: Boolean = false,
    /** True when the database already holds a record for this employee on the selected date. */
    val alreadyRecorded: Boolean = false
)

data class AttendanceUiState(
    val workDate: LocalDate = LocalDate.now(),
    val drafts: List<AttendanceDraft> = emptyList(),
    val rows: List<AttendanceRow> = emptyList(),
    val employees: List<EmployeeEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AttendanceViewModel(
    private val employeeId: Long?,
    private val attendanceRepository: AttendanceRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private data class Feedback(val error: String? = null, val info: String? = null, val isSaving: Boolean = false)

    private val _feedback = MutableStateFlow(Feedback())
    private val _workDate = MutableStateFlow(LocalDate.now())
    private val _drafts = MutableStateFlow<List<AttendanceDraft>>(emptyList())

    private val attendanceFlow = if (employeeId != null) attendanceRepository.observeByEmployee(employeeId) else attendanceRepository.observeAll()

    val uiState = combine(attendanceFlow, employeeRepository.observeAll(), _workDate, _drafts, _feedback) { records, employees, workDate, drafts, feedback ->
        val rows = records.sortedByDescending { it.workDate }.map { r ->
            AttendanceRow(r, employees.firstOrNull { it.id == r.employeeId }?.name ?: "Karyawan #${r.employeeId}")
        }
        val marked = drafts.map { draft ->
            draft.copy(alreadyRecorded = records.any { it.employeeId == draft.employeeId && it.workDate == workDate })
        }
        AttendanceUiState(
            workDate = workDate,
            drafts = marked,
            rows = rows,
            employees = employees,
            isLoading = false,
            isSaving = feedback.isSaving,
            errorMessage = feedback.error,
            infoMessage = feedback.info
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AttendanceUiState())

    /** The date every draft is saved under; the dialog only shows it as a label. */
    fun setWorkDate(date: LocalDate) {
        _workDate.value = date
    }

    /**
     * Appends active employees that are not on the list yet. Lines already on the list keep their
     * position and every edit, so tapping this a second time never resets anything.
     */
    fun loadEmployees() {
        viewModelScope.launch {
            runCatching {
                val candidates = employeeRepository.observeAll().first()
                    .filter { it.status == EmployeeStatus.ACTIVE }
                    .filter { employeeId == null || it.id == employeeId }
                    .sortedBy { it.name }
                val onList = _drafts.value.map { it.employeeId }.toSet()
                val added = candidates
                    .filterNot { it.id in onList }
                    .map { AttendanceDraft(employeeId = it.id, employeeName = it.name, nik = it.nik) }
                _drafts.update { it + added }
                _feedback.value = Feedback(
                    info = when {
                        candidates.isEmpty() -> "Tidak ada karyawan aktif."
                        added.isEmpty() -> "Daftar sudah memuat semua karyawan aktif (${candidates.size})."
                        else -> "${added.size} karyawan ditambahkan ke daftar."
                    }
                )
            }.onFailure { e -> _feedback.value = Feedback(error = e.message ?: "Gagal memuat karyawan aktif") }
        }
    }

    fun removeDraft(employeeIdToRemove: Long) {
        _drafts.update { list -> list.filterNot { it.employeeId == employeeIdToRemove } }
    }

    fun clearDrafts() {
        _drafts.value = emptyList()
        _feedback.value = Feedback(info = "Daftar dikosongkan.")
    }

    fun editDraft(
        employeeIdToEdit: Long,
        status: AttendanceStatus,
        lateMinutes: Int,
        earlyLeaveMinutes: Int,
        notes: String?
    ) {
        _drafts.update { list ->
            list.map { draft ->
                if (draft.employeeId != employeeIdToEdit) draft else draft.copy(
                    status = status,
                    lateMinutes = lateMinutes,
                    earlyLeaveMinutes = earlyLeaveMinutes,
                    notes = notes?.ifBlank { null },
                    edited = true
                )
            }
        }
    }

    /** Saves every draft under the selected date, updating a record that already exists for that day. */
    fun saveAll() {
        val drafts = _drafts.value
        if (drafts.isEmpty() || _feedback.value.isSaving) return
        val date = _workDate.value
        viewModelScope.launch {
            _feedback.value = Feedback(isSaving = true)
            var inserted = 0
            var updated = 0
            val failed = mutableListOf<String>()
            drafts.forEach { draft ->
                runCatching {
                    val existing = attendanceRepository.getByEmployeeAndDate(draft.employeeId, date)
                    if (existing == null) {
                        attendanceRepository.insert(
                            AttendanceEntity(
                                employeeId = draft.employeeId,
                                workDate = date,
                                status = draft.status,
                                lateMinutes = draft.lateMinutes,
                                earlyLeaveMinutes = draft.earlyLeaveMinutes,
                                notes = draft.notes?.ifBlank { null }
                            )
                        )
                        inserted++
                    } else {
                        attendanceRepository.update(
                            existing.copy(
                                status = draft.status,
                                lateMinutes = draft.lateMinutes,
                                earlyLeaveMinutes = draft.earlyLeaveMinutes,
                                notes = draft.notes?.ifBlank { null }
                            )
                        )
                        updated++
                    }
                }.onFailure { failed += draft.employeeName }
            }
            _feedback.value = Feedback(
                info = "${date.toDisplay()}: $inserted baru, $updated diperbarui.".takeIf { inserted + updated > 0 },
                error = "Gagal menyimpan: ${failed.joinToString()}".takeIf { failed.isNotEmpty() }
            )
        }
    }

    fun clearError() {
        _feedback.update { it.copy(error = null) }
    }

    fun clearInfo() {
        _feedback.update { it.copy(info = null) }
    }
}
