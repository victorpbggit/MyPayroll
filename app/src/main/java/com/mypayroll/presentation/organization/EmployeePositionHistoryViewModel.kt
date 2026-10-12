package com.mypayroll.presentation.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.DepartmentEntity
import com.mypayroll.data.local.entity.EmployeePositionHistoryEntity
import com.mypayroll.data.local.entity.PositionEntity
import com.mypayroll.domain.repository.DepartmentRepository
import com.mypayroll.domain.repository.EmployeePositionHistoryRepository
import com.mypayroll.domain.repository.PositionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PositionHistoryRow(val entry: EmployeePositionHistoryEntity, val positionName: String, val departmentName: String)

data class PositionHistoryUiState(
    val rows: List<PositionHistoryRow> = emptyList(),
    val positions: List<PositionEntity> = emptyList(),
    val departments: List<DepartmentEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/** A "transfer / promotion" closes the currently open history row (endDate = new start - 1 day)
 * and opens a new one, instead of mutating the old row's position/department in place - so the
 * change history stays intact. */
class EmployeePositionHistoryViewModel(
    private val employeeId: Long,
    private val repository: EmployeePositionHistoryRepository,
    private val positionRepository: PositionRepository,
    private val departmentRepository: DepartmentRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState = combine(
        repository.observeByEmployee(employeeId), positionRepository.observeAll(), departmentRepository.observeAll(), _errorMessage
    ) { entries, positions, departments, error ->
        val rows = entries.sortedByDescending { it.startDate }.map { e ->
            PositionHistoryRow(
                e,
                positions.firstOrNull { it.id == e.positionId }?.name ?: "-",
                departments.firstOrNull { it.id == e.departmentId }?.name ?: "-"
            )
        }
        PositionHistoryUiState(rows, positions, departments, isLoading = false, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PositionHistoryUiState())

    fun transfer(positionId: Long, departmentId: Long, supervisorId: Long?, startDate: LocalDate, reason: String?) {
        viewModelScope.launch {
            runCatching {
                val open = uiState.value.rows.map { it.entry }.firstOrNull { it.endDate == null }
                if (open != null && open.startDate.isBefore(startDate)) {
                    repository.update(open.copy(endDate = startDate.minusDays(1)))
                }
                repository.insert(
                    EmployeePositionHistoryEntity(
                        employeeId = employeeId, positionId = positionId, departmentId = departmentId,
                        supervisorId = supervisorId, startDate = startDate, reason = reason?.ifBlank { null }
                    )
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan perubahan posisi" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
