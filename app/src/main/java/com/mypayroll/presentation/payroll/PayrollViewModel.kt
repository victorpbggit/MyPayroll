package com.mypayroll.presentation.payroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import com.mypayroll.domain.repository.PayrollPeriodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class PayrollListUiState(
    val periods: List<PayrollPeriodEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class PayrollViewModel(private val repository: PayrollPeriodRepository) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState = combine(repository.observeAll(), _errorMessage) { periods, error ->
        PayrollListUiState(periods.sortedByDescending { it.startDate }, isLoading = false, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PayrollListUiState())

    fun createPeriod(code: String, name: String, start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            runCatching {
                repository.insert(PayrollPeriodEntity(code = code, name = name, startDate = start, endDate = end, createdAt = LocalDateTime.now()))
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal membuat periode payroll (kode mungkin sudah dipakai)" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
