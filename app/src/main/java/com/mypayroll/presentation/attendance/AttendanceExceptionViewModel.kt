package com.mypayroll.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.ApprovalStatus
import com.mypayroll.data.local.entity.AttendanceExceptionEntity
import com.mypayroll.data.local.entity.AttendanceExceptionType
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.domain.repository.AttendanceExceptionRepository
import com.mypayroll.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AttendanceExceptionRow(val exception: AttendanceExceptionEntity, val employeeName: String)

data class AttendanceExceptionUiState(
    val rows: List<AttendanceExceptionRow> = emptyList(),
    val employees: List<EmployeeEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class AttendanceExceptionViewModel(
    private val employeeId: Long?,
    private val repository: AttendanceExceptionRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val flow = if (employeeId != null) repository.observeByEmployee(employeeId) else repository.observeAll()

    val uiState = combine(flow, employeeRepository.observeAll(), _errorMessage) { records, employees, error ->
        val rows = records.sortedByDescending { it.date }.map { r ->
            AttendanceExceptionRow(r, employees.firstOrNull { it.id == r.employeeId }?.name ?: "Karyawan #${r.employeeId}")
        }
        AttendanceExceptionUiState(rows, employees, isLoading = false, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AttendanceExceptionUiState())

    fun submit(employeeIdForNew: Long, date: LocalDate, type: AttendanceExceptionType, reason: String?) {
        viewModelScope.launch {
            runCatching {
                repository.insert(AttendanceExceptionEntity(employeeId = employeeIdForNew, date = date, type = type, reason = reason?.ifBlank { null }))
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan pengecualian absensi" }
        }
    }

    fun decide(exception: AttendanceExceptionEntity, approve: Boolean, approverId: Long?) {
        viewModelScope.launch {
            runCatching {
                repository.update(exception.copy(status = if (approve) ApprovalStatus.APPROVED else ApprovalStatus.REJECTED, approvedBy = approverId))
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal memperbarui status" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
