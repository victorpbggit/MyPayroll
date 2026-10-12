package com.mypayroll.presentation.payroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.AttendanceStatus
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.data.local.entity.EmployeeStatus
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import com.mypayroll.domain.payroll.AdditionalEarning
import com.mypayroll.domain.payroll.CalculatePayrollUseCase
import com.mypayroll.domain.payroll.EmployeePayrollType
import com.mypayroll.domain.payroll.OvertimeEvent
import com.mypayroll.domain.payroll.PayrollCalculationInput
import com.mypayroll.domain.payroll.PayrollCalculationResult
import com.mypayroll.domain.payroll.PayrollDeduction
import com.mypayroll.domain.repository.AttendanceRepository
import com.mypayroll.domain.repository.EmployeeRepository
import com.mypayroll.domain.repository.EmployeeSalaryHistoryRepository
import com.mypayroll.domain.repository.PayrollSetupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One manually entered overtime event in the calculator form: hours, and the PL nominal if the event exceeds 2 hours. */
data class OvertimeEventDraft(val hours: Int, val overtimePremiumPerEvent: Long)

/** One manually entered flexible earning/deduction component (name + nominal). */
data class NamedAmountDraft(val name: String, val amount: Long)

data class PayrollCalculatorFormState(
    val employeeType: EmployeePayrollType = EmployeePayrollType.NON_MANAGER,
    val basicSalaryMonthly: String = "0",
    val positionAllowanceMonthly: String = "0",
    val mealAllowanceMonthlyManager: String = "0",
    val attendancePremiumMonthlyManager: String = "0",
    val mealAllowanceDaily: String = "0",
    val attendancePremiumDaily: String = "0",
    val scheduledWorkDays: String = "0",
    val attendanceDays: String = "0",
    val overtimeHourlyRate: String = "0",
    val overtimeEvents: List<OvertimeEventDraft> = emptyList(),
    val additionalEarnings: List<NamedAmountDraft> = emptyList(),
    val deductions: List<NamedAmountDraft> = emptyList()
)

data class PayrollCalculatorUiState(
    val period: PayrollPeriodEntity? = null,
    val employees: List<EmployeeEntity> = emptyList(),
    val selectedEmployee: EmployeeEntity? = null,
    val isPrefilling: Boolean = false,
    val form: PayrollCalculatorFormState = PayrollCalculatorFormState(),
    val defaultOvertimePremiumPerEvent: Long = 0L,
    val result: PayrollCalculationResult? = null,
    val errorMessage: String? = null
)

/**
 * Drives the detailed GP/TJ/UM/PH/UL/PL calculator backed by [CalculatePayrollUseCase].
 *
 * This is computation-only: [PayrollResultEntity] has no columns for UM, PH, UL, and PL as
 * separate amounts, so a result here is never persisted (see the disclaimer shown in the UI).
 * Basic salary and hari hadir are pre-filled from real data (latest salary history, actual
 * attendance records for the period); every other component (TJ, UM, PH, UL, PL) has no
 * dedicated source in the current schema, so it is pre-filled from the global defaults set
 * in Payroll Setup ([PayrollSetupRepository]) instead - every field stays editable per employee.
 */
class PayrollCalculatorViewModel(
    period: PayrollPeriodEntity,
    private val employeeRepository: EmployeeRepository,
    private val salaryHistoryRepository: EmployeeSalaryHistoryRepository,
    private val attendanceRepository: AttendanceRepository,
    private val payrollSetupRepository: PayrollSetupRepository,
    private val calculatePayrollUseCase: CalculatePayrollUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PayrollCalculatorUiState(period = period))
    val uiState = combine(employeeRepository.observeAll(), _state) { employees, state ->
        state.copy(employees = employees.filter { it.status == EmployeeStatus.ACTIVE }.sortedBy { it.name })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun selectEmployee(employee: EmployeeEntity) {
        val period = _state.value.period ?: return
        _state.value = _state.value.copy(selectedEmployee = employee, result = null, isPrefilling = true)
        viewModelScope.launch {
            val config = payrollSetupRepository.getConfig()

            val latestSalary = salaryHistoryRepository.observeByEmployee(employee.id).first()
                .filter { !it.effectiveDate.isAfter(period.endDate) }
                .maxByOrNull { it.effectiveDate }?.basicSalary ?: config.basicSalaryMonthly

            val periodAttendance = attendanceRepository.observeByEmployee(employee.id).first()
                .filter { !it.workDate.isBefore(period.startDate) && !it.workDate.isAfter(period.endDate) }
            val attendanceDays = periodAttendance.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE }

            val calendarDays = (period.endDate.toEpochDay() - period.startDate.toEpochDay() + 1).toInt()

            _state.value = _state.value.copy(
                isPrefilling = false,
                defaultOvertimePremiumPerEvent = config.overtimePremiumPerEvent,
                form = PayrollCalculatorFormState(
                    basicSalaryMonthly = latestSalary.toString(),
                    positionAllowanceMonthly = config.positionAllowanceMonthly.toString(),
                    mealAllowanceMonthlyManager = config.mealAllowanceMonthlyManager.toString(),
                    attendancePremiumMonthlyManager = config.attendancePremiumMonthlyManager.toString(),
                    mealAllowanceDaily = config.mealAllowanceDaily.toString(),
                    attendancePremiumDaily = config.attendancePremiumDaily.toString(),
                    scheduledWorkDays = calendarDays.toString(),
                    attendanceDays = attendanceDays.coerceAtMost(calendarDays).toString(),
                    overtimeHourlyRate = config.overtimeHourlyRate.toString()
                )
            )
        }
    }

    fun updateForm(transform: (PayrollCalculatorFormState) -> PayrollCalculatorFormState) {
        _state.value = _state.value.copy(form = transform(_state.value.form))
    }

    fun addOvertimeEvent(hours: Int, premium: Long) {
        updateForm { it.copy(overtimeEvents = it.overtimeEvents + OvertimeEventDraft(hours, premium)) }
    }

    fun removeOvertimeEvent(index: Int) {
        updateForm { it.copy(overtimeEvents = it.overtimeEvents.filterIndexed { i, _ -> i != index }) }
    }

    fun addAdditionalEarning(name: String, amount: Long) {
        updateForm { it.copy(additionalEarnings = it.additionalEarnings + NamedAmountDraft(name, amount)) }
    }

    fun removeAdditionalEarning(index: Int) {
        updateForm { it.copy(additionalEarnings = it.additionalEarnings.filterIndexed { i, _ -> i != index }) }
    }

    fun addDeduction(name: String, amount: Long) {
        updateForm { it.copy(deductions = it.deductions + NamedAmountDraft(name, amount)) }
    }

    fun removeDeduction(index: Int) {
        updateForm { it.copy(deductions = it.deductions.filterIndexed { i, _ -> i != index }) }
    }

    fun calculate() {
        val form = _state.value.form
        val input = PayrollCalculationInput(
            employeeType = form.employeeType,
            basicSalaryMonthly = form.basicSalaryMonthly.toLongOrNull() ?: 0L,
            positionAllowanceMonthly = form.positionAllowanceMonthly.toLongOrNull() ?: 0L,
            mealAllowanceMonthlyManager = form.mealAllowanceMonthlyManager.toLongOrNull() ?: 0L,
            attendancePremiumMonthlyManager = form.attendancePremiumMonthlyManager.toLongOrNull() ?: 0L,
            mealAllowanceDaily = form.mealAllowanceDaily.toLongOrNull() ?: 0L,
            attendancePremiumDaily = form.attendancePremiumDaily.toLongOrNull() ?: 0L,
            scheduledWorkDays = form.scheduledWorkDays.toIntOrNull() ?: 0,
            attendanceDays = form.attendanceDays.toIntOrNull() ?: 0,
            overtimeHourlyRate = form.overtimeHourlyRate.toLongOrNull() ?: 0L,
            overtimeEvents = form.overtimeEvents.map { OvertimeEvent(it.hours, it.overtimePremiumPerEvent) },
            additionalEarnings = form.additionalEarnings.map { AdditionalEarning(it.name, it.amount) },
            deductions = form.deductions.map { PayrollDeduction(it.name, it.amount) }
        )
        runCatching { calculatePayrollUseCase(input) }
            .onSuccess { result -> _state.value = _state.value.copy(result = result, errorMessage = null) }
            .onFailure { e -> _state.value = _state.value.copy(result = null, errorMessage = e.message ?: "Input tidak valid") }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
