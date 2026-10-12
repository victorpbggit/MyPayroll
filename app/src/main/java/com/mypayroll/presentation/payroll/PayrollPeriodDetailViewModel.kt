package com.mypayroll.presentation.payroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import com.mypayroll.data.local.entity.PayrollPeriodStatus
import com.mypayroll.data.local.entity.PayrollResultEntity
import com.mypayroll.domain.model.PayrollLineItem
import com.mypayroll.domain.model.PayrollPolicy
import com.mypayroll.domain.repository.PayrollResultRepository
import com.mypayroll.domain.usecase.ApprovePayrollUseCase
import com.mypayroll.domain.usecase.FinalizePayrollUseCase
import com.mypayroll.domain.usecase.PreviewPayrollUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class PayrollPeriodDetailUiState(
    val period: PayrollPeriodEntity,
    val preview: List<PayrollLineItem> = emptyList(),
    val savedResults: List<PayrollResultEntity> = emptyList(),
    val isCalculating: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class PayrollPeriodDetailViewModel(
    initialPeriod: PayrollPeriodEntity,
    private val previewPayrollUseCase: PreviewPayrollUseCase,
    private val approvePayrollUseCase: ApprovePayrollUseCase,
    private val finalizePayrollUseCase: FinalizePayrollUseCase,
    private val payrollResultRepository: PayrollResultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PayrollPeriodDetailUiState(period = initialPeriod))
    val uiState = _uiState.asStateFlow()

    init {
        payrollResultRepository.observeByPeriod(initialPeriod.id)
            .onEach { results -> _uiState.value = _uiState.value.copy(savedResults = results) }
            .launchIn(viewModelScope)
    }

    fun runPreview(policy: PayrollPolicy) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCalculating = true)
            runCatching { previewPayrollUseCase(_uiState.value.period, policy) }
                .onSuccess { lines -> _uiState.value = _uiState.value.copy(preview = lines, infoMessage = "Pratinjau dihitung ulang (belum disimpan).", isCalculating = false) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Gagal menghitung pratinjau payroll", isCalculating = false) }
        }
    }

    fun approve(approverId: Long?) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current.preview.isEmpty()) {
                _uiState.value = current.copy(errorMessage = "Hitung pratinjau terlebih dahulu sebelum menyetujui.")
                return@launch
            }
            runCatching { approvePayrollUseCase(current.period, current.preview, approverId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        period = _uiState.value.period.copy(status = PayrollPeriodStatus.APPROVED),
                        infoMessage = "Payroll disetujui dan tersimpan."
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Gagal menyetujui payroll") }
        }
    }

    fun finalize(finalizedBy: Long?) {
        viewModelScope.launch {
            runCatching { finalizePayrollUseCase(_uiState.value.period, finalizedBy) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        period = _uiState.value.period.copy(status = PayrollPeriodStatus.FINALIZED),
                        infoMessage = "Payroll difinalisasi."
                    )
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Gagal memfinalisasi payroll") }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, infoMessage = null)
    }
}
