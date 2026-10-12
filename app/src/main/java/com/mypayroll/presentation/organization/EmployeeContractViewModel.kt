package com.mypayroll.presentation.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.ContractStatus
import com.mypayroll.data.local.entity.ContractTypeEntity
import com.mypayroll.data.local.entity.EmployeeContractEntity
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.domain.repository.ContractTypeRepository
import com.mypayroll.domain.repository.EmployeeContractRepository
import com.mypayroll.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ContractRow(
    val contract: EmployeeContractEntity,
    val employeeName: String,
    val contractTypeName: String,
    val daysToExpiry: Long?
)

data class EmployeeContractUiState(
    val rows: List<ContractRow> = emptyList(),
    val employees: List<EmployeeEntity> = emptyList(),
    val contractTypes: List<ContractTypeEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/** Shows either every contract (employeeId = null) or just one employee's contracts. */
class EmployeeContractViewModel(
    private val employeeId: Long?,
    private val contractRepository: EmployeeContractRepository,
    private val employeeRepository: EmployeeRepository,
    private val contractTypeRepository: ContractTypeRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val today = LocalDate.now()

    private val contractsFlow = if (employeeId != null) {
        contractRepository.observeByEmployee(employeeId)
    } else {
        contractRepository.observeAll()
    }

    val uiState = combine(
        contractsFlow,
        employeeRepository.observeAll(),
        contractTypeRepository.observeAll(),
        _errorMessage
    ) { contracts, employees, contractTypes, error ->
        val rows = contracts.sortedByDescending { it.startDate }.map { c ->
            ContractRow(
                contract = c,
                employeeName = employees.firstOrNull { it.id == c.employeeId }?.name ?: "Karyawan #${c.employeeId}",
                contractTypeName = contractTypes.firstOrNull { it.id == c.contractTypeId }?.name ?: "-",
                daysToExpiry = c.endDate?.let { java.time.temporal.ChronoUnit.DAYS.between(today, it) }
            )
        }
        EmployeeContractUiState(rows, employees, contractTypes, isLoading = false, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmployeeContractUiState())

    fun addContract(employeeIdForNew: Long, contractNumber: String, contractTypeId: Long, startDate: LocalDate, endDate: LocalDate?) {
        viewModelScope.launch {
            runCatching {
                contractRepository.insert(
                    EmployeeContractEntity(
                        employeeId = employeeIdForNew,
                        contractNumber = contractNumber,
                        contractTypeId = contractTypeId,
                        startDate = startDate,
                        endDate = endDate
                    )
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan kontrak" }
        }
    }

    fun terminate(contract: EmployeeContractEntity, reason: String) {
        viewModelScope.launch {
            runCatching {
                contractRepository.update(
                    contract.copy(status = ContractStatus.TERMINATED, terminationDate = today, terminationReason = reason)
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal mengubah status kontrak" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
