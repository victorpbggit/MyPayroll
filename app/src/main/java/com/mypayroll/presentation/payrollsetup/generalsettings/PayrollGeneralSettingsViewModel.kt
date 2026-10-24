package com.mypayroll.presentation.payrollsetup.generalsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.domain.model.PayrollGeneralSettings
import com.mypayroll.domain.repository.PayrollGeneralSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PayrollGeneralSettingsUiState(
    val currencyText: String = "",
    val daysPerMonthText: String = "",
    val daysPerWeekText: String = "",
    val hoursPerWeekText: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedMessage: String? = null,
    val errorMessage: String? = null
)

/** Draft text per field, pola yang sama dengan [com.mypayroll.presentation.payrollsetup.latepenalty.LatePenaltyViewModel]. */
private data class LocalState(
    val currencyText: String? = null,
    val daysPerMonthText: String? = null,
    val daysPerWeekText: String? = null,
    val hoursPerWeekText: String? = null,
    val error: String? = null,
    val saved: String? = null,
    val isSaving: Boolean = false
)

/**
 * Curr/DM/DW/HW dipakai untuk menurunkan GPPH/GPPJ di tab Gaji Pokok pada Kompensasi
 * (lihat [com.mypayroll.domain.payroll.gajiPokokPerHari]/[com.mypayroll.domain.payroll.gajiPokokPerJam]).
 * Validasi (>0, currency tidak kosong) dijalankan di [PayrollGeneralSettingsRepository.saveSettings]
 * (lapisan bisnis), bukan hanya UI. Simpan hanya lewat tombol "Simpan", tidak ada autosave.
 */
class PayrollGeneralSettingsViewModel(
    private val repository: PayrollGeneralSettingsRepository
) : ViewModel() {

    private val _local = MutableStateFlow(LocalState())

    val uiState = combine(repository.observeSettings(), _local) { persisted, local ->
        PayrollGeneralSettingsUiState(
            currencyText = local.currencyText ?: persisted.currency,
            daysPerMonthText = local.daysPerMonthText ?: persisted.daysPerMonth.toString(),
            daysPerWeekText = local.daysPerWeekText ?: persisted.daysPerWeek.toString(),
            hoursPerWeekText = local.hoursPerWeekText ?: persisted.hoursPerWeek.toString(),
            isLoading = false,
            isSaving = local.isSaving,
            savedMessage = local.saved,
            errorMessage = local.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PayrollGeneralSettingsUiState())

    fun updateCurrencyText(text: String) {
        _local.value = _local.value.copy(currencyText = text, error = null, saved = null)
    }

    fun updateDaysPerMonthText(text: String) {
        _local.value = _local.value.copy(daysPerMonthText = text, error = null, saved = null)
    }

    fun updateDaysPerWeekText(text: String) {
        _local.value = _local.value.copy(daysPerWeekText = text, error = null, saved = null)
    }

    fun updateHoursPerWeekText(text: String) {
        _local.value = _local.value.copy(hoursPerWeekText = text, error = null, saved = null)
    }

    fun save() {
        viewModelScope.launch { persist() }
    }

    private suspend fun persist() {
        val local = _local.value
        val persisted = repository.getSettings()

        val currency = local.currencyText?.ifBlank { null }
        val daysPerMonth = local.daysPerMonthText?.toIntOrNull()
        val daysPerWeek = local.daysPerWeekText?.toIntOrNull()
        val hoursPerWeek = local.hoursPerWeekText?.toIntOrNull()

        val invalid = listOfNotNull(
            if (local.currencyText != null && currency == null) "Currency" else null,
            if (local.daysPerMonthText != null && daysPerMonth == null) "Days per month" else null,
            if (local.daysPerWeekText != null && daysPerWeek == null) "Days per week" else null,
            if (local.hoursPerWeekText != null && hoursPerWeek == null) "Hours per week" else null
        )
        if (invalid.isNotEmpty()) {
            _local.value = _local.value.copy(
                isSaving = false,
                error = "${invalid.joinToString(", ")} belum diisi dengan nilai yang valid - belum tersimpan."
            )
            return
        }

        _local.value = _local.value.copy(isSaving = true, error = null)
        val candidate = PayrollGeneralSettings(
            currency = currency ?: persisted.currency,
            daysPerMonth = daysPerMonth ?: persisted.daysPerMonth,
            daysPerWeek = daysPerWeek ?: persisted.daysPerWeek,
            hoursPerWeek = hoursPerWeek ?: persisted.hoursPerWeek
        )
        runCatching { repository.saveSettings(candidate) }
            .onSuccess { _local.value = _local.value.copy(isSaving = false, error = null, saved = "Pengaturan umum payroll disimpan.") }
            .onFailure { e -> _local.value = _local.value.copy(isSaving = false, error = e.message ?: "Gagal menyimpan.") }
    }

    fun clearMessages() {
        _local.value = _local.value.copy(error = null, saved = null)
    }
}
