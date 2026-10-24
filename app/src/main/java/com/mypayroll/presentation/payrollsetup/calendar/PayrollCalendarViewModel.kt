package com.mypayroll.presentation.payrollsetup.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.domain.payroll.PayrollDayStatus
import com.mypayroll.domain.payroll.defaultPayrollDayStatus
import com.mypayroll.domain.repository.PayrollCalendarRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PayrollCalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val statuses: Map<LocalDate, PayrollDayStatus> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedMessage: String? = null,
    val errorMessage: String? = null
)

class PayrollCalendarViewModel(
    private val repository: PayrollCalendarRepository
) : ViewModel() {

    private data class Feedback(val error: String? = null, val saved: String? = null, val isSaving: Boolean = false)

    private val _month = MutableStateFlow(YearMonth.now())
    private val _feedback = MutableStateFlow(Feedback())

    val uiState = combine(_month, repository.observeOverrides(), _feedback) { month, overrides, feedback ->
        val statuses = (1..month.lengthOfMonth()).associate { day ->
            val date = month.atDay(day)
            date to (overrides[date] ?: defaultPayrollDayStatus(date))
        }
        PayrollCalendarUiState(
            month = month,
            statuses = statuses,
            isLoading = false,
            isSaving = feedback.isSaving,
            savedMessage = feedback.saved,
            errorMessage = feedback.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PayrollCalendarUiState())

    fun previousMonth() {
        _month.value = _month.value.minusMonths(1)
    }

    fun nextMonth() {
        _month.value = _month.value.plusMonths(1)
    }

    /** Menyimpan status [date] secara persisten lewat repository - bukan hanya mengubah warna di state. */
    fun setStatus(date: LocalDate, status: PayrollDayStatus) {
        viewModelScope.launch {
            _feedback.value = Feedback(isSaving = true)
            runCatching { repository.setStatus(date, status) }
                .onSuccess {
                    _feedback.value = Feedback(saved = "Status ${date} disimpan sebagai ${status.label()}.")
                }
                .onFailure { e ->
                    _feedback.value = Feedback(error = e.message ?: "Gagal menyimpan status tanggal.")
                }
        }
    }

    fun clearMessages() {
        _feedback.value = Feedback()
    }
}

fun PayrollDayStatus.label(): String = when (this) {
    PayrollDayStatus.WORKDAY -> "Hari Kerja"
    PayrollDayStatus.HOLIDAY -> "Holiday"
}
