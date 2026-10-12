package com.mypayroll.presentation.discipline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.DisciplinaryActionType
import com.mypayroll.data.local.entity.EmployeeDisciplinaryActionEntity
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.domain.repository.EmployeeDisciplinaryActionRepository
import com.mypayroll.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DisciplineRow(val action: EmployeeDisciplinaryActionEntity, val employeeName: String)

data class DisciplineUiState(
    val rows: List<DisciplineRow> = emptyList(),
    val employees: List<EmployeeEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class DisciplineViewModel(
    private val employeeId: Long?,
    private val repository: EmployeeDisciplinaryActionRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val flow = if (employeeId != null) repository.observeByEmployee(employeeId) else repository.observeAll()

    val uiState = combine(flow, employeeRepository.observeAll(), _errorMessage) { actions, employees, error ->
        val rows = actions.sortedByDescending { it.incidentDate }.map { a ->
            DisciplineRow(a, employees.firstOrNull { it.id == a.employeeId }?.name ?: "Karyawan #${a.employeeId}")
        }
        DisciplineUiState(rows, employees, isLoading = false, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DisciplineUiState())

    fun add(employeeIdForNew: Long, incidentDate: LocalDate, actionDate: LocalDate, type: DisciplinaryActionType, description: String, issuedBy: Long?) {
        viewModelScope.launch {
            runCatching {
                repository.insert(
                    EmployeeDisciplinaryActionEntity(
                        employeeId = employeeIdForNew, incidentDate = incidentDate, actionDate = actionDate,
                        actionType = type, description = description, issuedBy = issuedBy
                    )
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan catatan disiplin" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
