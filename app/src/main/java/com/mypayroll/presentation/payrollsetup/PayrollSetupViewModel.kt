package com.mypayroll.presentation.payrollsetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.domain.model.PayrollSetupConfig
import com.mypayroll.domain.repository.PayrollSetupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PayrollSetupUiState(
    val config: PayrollSetupConfig = PayrollSetupConfig(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * Mengatur nilai default GP/TJ/UM/PH/UL/PL lewat [PayrollSetupRepository]. Nilai di sini
 * hanya dipakai sebagai nilai awal oleh Kalkulator Payroll (lihat PayrollCalculatorViewModel) -
 * perhitungan sebenarnya tetap lewat CalculatePayrollUseCase/PayrollCalculator, tidak ada
 * formula payroll baru di layer UI.
 */
class PayrollSetupViewModel(private val repository: PayrollSetupRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PayrollSetupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(config = repository.getConfig(), isLoading = false)
        }
    }

    fun updateConfig(transform: (PayrollSetupConfig) -> PayrollSetupConfig) {
        _uiState.value = _uiState.value.copy(config = transform(_uiState.value.config), savedMessage = null)
    }

    fun save() {
        val config = _uiState.value.config
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            runCatching { repository.saveConfig(config) }
                .onSuccess { _uiState.value = _uiState.value.copy(isSaving = false, savedMessage = "Payroll Setup disimpan.", errorMessage = null) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message ?: "Gagal menyimpan Payroll Setup") }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(savedMessage = null, errorMessage = null)
    }
}
