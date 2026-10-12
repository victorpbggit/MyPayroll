package com.mypayroll.presentation.compensation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.CalculationType
import com.mypayroll.data.local.entity.EmployeeIncentiveEntity
import com.mypayroll.data.local.entity.EmployeePayComponentHistoryEntity
import com.mypayroll.data.local.entity.EmployeeSalaryHistoryEntity
import com.mypayroll.data.local.entity.PayComponentEntity
import com.mypayroll.data.local.entity.SalaryType
import com.mypayroll.domain.repository.EmployeeIncentiveRepository
import com.mypayroll.domain.repository.EmployeePayComponentHistoryRepository
import com.mypayroll.domain.repository.EmployeeSalaryHistoryRepository
import com.mypayroll.domain.repository.PayComponentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PayComponentHistoryRow(val entry: EmployeePayComponentHistoryEntity, val componentName: String)
data class IncentiveRow(val entry: EmployeeIncentiveEntity, val componentName: String)

data class EmployeeCompensationUiState(
    val salaryHistory: List<EmployeeSalaryHistoryEntity> = emptyList(),
    val payComponentHistory: List<PayComponentHistoryRow> = emptyList(),
    val incentives: List<IncentiveRow> = emptyList(),
    val payComponents: List<PayComponentEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class EmployeeCompensationViewModel(
    private val employeeId: Long,
    private val salaryHistoryRepository: EmployeeSalaryHistoryRepository,
    private val payComponentHistoryRepository: EmployeePayComponentHistoryRepository,
    private val incentiveRepository: EmployeeIncentiveRepository,
    private val payComponentRepository: PayComponentRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)

    private val part1 = combine(
        salaryHistoryRepository.observeByEmployee(employeeId),
        payComponentHistoryRepository.observeByEmployee(employeeId),
        payComponentRepository.observeAll()
    ) { salary, components, payComponents ->
        Triple(salary, components, payComponents)
    }

    val uiState = combine(part1, incentiveRepository.observeByEmployee(employeeId), _errorMessage) { (salary, components, payComponents), incentives, error ->
        EmployeeCompensationUiState(
            salaryHistory = salary.sortedByDescending { it.effectiveDate },
            payComponentHistory = components.sortedByDescending { it.effectiveDate }.map { c ->
                PayComponentHistoryRow(c, payComponents.firstOrNull { it.id == c.payComponentId }?.name ?: "-")
            },
            incentives = incentives.sortedByDescending { it.date }.map { i ->
                IncentiveRow(i, payComponents.firstOrNull { it.id == i.payComponentId }?.name ?: "-")
            },
            payComponents = payComponents,
            isLoading = false,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmployeeCompensationUiState())

    fun addSalary(effectiveDate: LocalDate, basicSalary: Long, salaryType: SalaryType, reason: String?) {
        viewModelScope.launch {
            runCatching {
                salaryHistoryRepository.insert(
                    EmployeeSalaryHistoryEntity(employeeId = employeeId, effectiveDate = effectiveDate, basicSalary = basicSalary, salaryType = salaryType, reason = reason?.ifBlank { null })
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan riwayat gaji" }
        }
    }

    fun addPayComponent(payComponentId: Long, effectiveDate: LocalDate, amount: Long, calculationType: CalculationType, reason: String?) {
        viewModelScope.launch {
            runCatching {
                payComponentHistoryRepository.insert(
                    EmployeePayComponentHistoryEntity(
                        employeeId = employeeId, payComponentId = payComponentId, effectiveDate = effectiveDate,
                        amount = amount, calculationType = calculationType, reason = reason?.ifBlank { null }
                    )
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan komponen gaji karyawan" }
        }
    }

    fun addIncentive(payComponentId: Long, date: LocalDate, amount: Long, reason: String?) {
        viewModelScope.launch {
            runCatching {
                incentiveRepository.insert(
                    EmployeeIncentiveEntity(employeeId = employeeId, date = date, payComponentId = payComponentId, amount = amount, reason = reason?.ifBlank { null })
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan insentif" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
