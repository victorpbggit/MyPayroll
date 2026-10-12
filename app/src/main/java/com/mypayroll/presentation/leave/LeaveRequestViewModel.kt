package com.mypayroll.presentation.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.ApprovalStatus
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.data.local.entity.LeaveRequestEntity
import com.mypayroll.data.local.entity.LeaveTypeEntity
import com.mypayroll.domain.repository.EmployeeRepository
import com.mypayroll.domain.repository.LeaveRequestRepository
import com.mypayroll.domain.repository.LeaveTypeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class LeaveRequestRow(val request: LeaveRequestEntity, val employeeName: String, val leaveTypeName: String)

data class LeaveRequestUiState(
    val rows: List<LeaveRequestRow> = emptyList(),
    val employees: List<EmployeeEntity> = emptyList(),
    val leaveTypes: List<LeaveTypeEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/** Leave balances are intentionally not computed here: the schema has no leave-entitlement
 * table, so showing a balance would mean fabricating a number. Only request history/approval
 * is implemented, per the instruction to only show balances when backed by real data. */
class LeaveRequestViewModel(
    private val employeeId: Long?,
    private val repository: LeaveRequestRepository,
    private val employeeRepository: EmployeeRepository,
    private val leaveTypeRepository: LeaveTypeRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val flow = if (employeeId != null) repository.observeByEmployee(employeeId) else repository.observeAll()

    val uiState = combine(flow, employeeRepository.observeAll(), leaveTypeRepository.observeAll(), _errorMessage) { requests, employees, leaveTypes, error ->
        val rows = requests.sortedByDescending { it.startDate }.map { r ->
            LeaveRequestRow(
                r,
                employees.firstOrNull { it.id == r.employeeId }?.name ?: "Karyawan #${r.employeeId}",
                leaveTypes.firstOrNull { it.id == r.leaveTypeId }?.name ?: "-"
            )
        }
        LeaveRequestUiState(rows, employees, leaveTypes, isLoading = false, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LeaveRequestUiState())

    fun submit(employeeIdForNew: Long, leaveTypeId: Long, start: LocalDate, end: LocalDate, reason: String?) {
        viewModelScope.launch {
            runCatching {
                repository.insert(
                    LeaveRequestEntity(
                        employeeId = employeeIdForNew,
                        leaveTypeId = leaveTypeId,
                        startDate = start,
                        endDate = end,
                        requestedAt = LocalDateTime.now(),
                        reason = reason?.ifBlank { null }
                    )
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal mengajukan cuti" }
        }
    }

    fun decide(request: LeaveRequestEntity, approve: Boolean, approverId: Long?) {
        viewModelScope.launch {
            runCatching {
                repository.update(
                    request.copy(
                        status = if (approve) ApprovalStatus.APPROVED else ApprovalStatus.REJECTED,
                        approvedBy = approverId,
                        approvedAt = LocalDateTime.now()
                    )
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal memperbarui status cuti" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
