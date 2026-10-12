package com.mypayroll.presentation.overtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.data.local.entity.OvertimeAssignmentEntity
import com.mypayroll.data.local.entity.OvertimeAssignmentStatus
import com.mypayroll.data.local.entity.OvertimeRecordEntity
import com.mypayroll.data.local.entity.OvertimeRecordStatus
import com.mypayroll.domain.repository.EmployeeRepository
import com.mypayroll.domain.repository.OvertimeAssignmentRepository
import com.mypayroll.domain.repository.OvertimeRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class OvertimeAssignmentRow(
    val assignment: OvertimeAssignmentEntity,
    val employeeName: String,
    val records: List<OvertimeRecordEntity>
)

data class OvertimeUiState(
    val rows: List<OvertimeAssignmentRow> = emptyList(),
    val employees: List<EmployeeEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class OvertimeViewModel(
    private val employeeId: Long?,
    private val assignmentRepository: OvertimeAssignmentRepository,
    private val recordRepository: OvertimeRecordRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val assignmentFlow = if (employeeId != null) assignmentRepository.observeByEmployee(employeeId) else assignmentRepository.observeAll()

    val uiState = combine(assignmentFlow, recordRepository.observeAll(), employeeRepository.observeAll(), _errorMessage) { assignments, records, employees, error ->
        val rows = assignments.sortedByDescending { it.workDate }.map { a ->
            OvertimeAssignmentRow(a, employees.firstOrNull { it.id == a.employeeId }?.name ?: "Karyawan #${a.employeeId}", records.filter { it.assignmentId == a.id })
        }
        OvertimeUiState(rows, employees, isLoading = false, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OvertimeUiState())

    fun assign(employeeIdForNew: Long, date: LocalDate, start: LocalTime, end: LocalTime, description: String?) {
        viewModelScope.launch {
            runCatching {
                assignmentRepository.insert(
                    OvertimeAssignmentEntity(
                        employeeId = employeeIdForNew,
                        workDate = date,
                        plannedStart = start,
                        plannedEnd = end,
                        description = description?.ifBlank { null },
                        assignedAt = LocalDateTime.now()
                    )
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal membuat penugasan lembur" }
        }
    }

    /** Employee response to an assignment: accept or refuse (not the same as completing the work). */
    fun respond(assignment: OvertimeAssignmentEntity, accept: Boolean, rejectionReason: String? = null) {
        viewModelScope.launch {
            runCatching {
                assignmentRepository.update(
                    assignment.copy(
                        status = if (accept) OvertimeAssignmentStatus.ACCEPTED else OvertimeAssignmentStatus.REJECTED,
                        rejectedAt = if (accept) null else LocalDateTime.now(),
                        rejectionReason = if (accept) null else rejectionReason
                    )
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal memperbarui status penugasan" }
        }
    }

    /** Logs the actual overtime worked against an assignment - distinct from the plan. */
    fun recordActual(assignment: OvertimeAssignmentEntity, actualStart: LocalTime, actualEnd: LocalTime, ratePerMinute: Long) {
        viewModelScope.launch {
            runCatching {
                val minutes = Duration.between(actualStart, actualEnd).toMinutes().toInt().coerceAtLeast(0)
                recordRepository.insert(
                    OvertimeRecordEntity(
                        assignmentId = assignment.id,
                        actualStart = actualStart,
                        actualEnd = actualEnd,
                        approvedMinutes = minutes,
                        calculationAmount = minutes * ratePerMinute
                    )
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal mencatat lembur aktual" }
        }
    }

    fun decideRecord(record: OvertimeRecordEntity, approve: Boolean, approverId: Long?) {
        viewModelScope.launch {
            runCatching {
                recordRepository.update(record.copy(status = if (approve) OvertimeRecordStatus.APPROVED else OvertimeRecordStatus.REJECTED, approvedBy = approverId))
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal memperbarui status lembur" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
