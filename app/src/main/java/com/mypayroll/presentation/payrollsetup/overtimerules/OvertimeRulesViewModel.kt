package com.mypayroll.presentation.payrollsetup.overtimerules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.domain.model.OvertimeRuleConfig
import com.mypayroll.domain.repository.OvertimeRuleConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OvertimeRulesUiState(
    val rule1Text: String = "",
    val rule2Text: String = "",
    val holidayText: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * Draft text per field, kept as the literal string the user is typing (including "" while
 * backspacing) - never reconstructed from a parsed Int. Reconstructing from Int was a past bug:
 * an empty/invalid string failed to parse, so the update was dropped and the field froze at
 * its last valid single digit instead of going blank.
 */
private data class LocalState(
    val rule1Text: String? = null,
    val rule2Text: String? = null,
    val holidayText: String? = null,
    val error: String? = null,
    val saved: String? = null,
    val isSaving: Boolean = false
)

/**
 * Validasi minimum (Rule 1/2 >= 100) dijalankan di [OvertimeRuleConfigRepository.saveConfig]
 * (lapisan bisnis), bukan hanya di UI. Nilai di sini TERSIMPAN tetapi belum dipakai oleh
 * formula kalkulator payroll resmi, lihat catatan di [OvertimeRuleConfig].
 *
 * Simpan HANYA lewat tombol "Simpan" (tidak ada autosave - dicabut atas permintaan user).
 */
class OvertimeRulesViewModel(
    private val repository: OvertimeRuleConfigRepository
) : ViewModel() {

    private val _local = MutableStateFlow(LocalState())

    val uiState = combine(repository.observeConfig(), _local) { persisted, local ->
        OvertimeRulesUiState(
            rule1Text = local.rule1Text ?: persisted.rule1PercentMin100.toString(),
            rule2Text = local.rule2Text ?: persisted.rule2PercentMin100.toString(),
            holidayText = local.holidayText ?: persisted.holidayPercent.toString(),
            isLoading = false,
            isSaving = local.isSaving,
            savedMessage = local.saved,
            errorMessage = local.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OvertimeRulesUiState())

    fun updateRule1Text(text: String) {
        _local.value = _local.value.copy(rule1Text = text, error = null, saved = null)
    }

    fun updateRule2Text(text: String) {
        _local.value = _local.value.copy(rule2Text = text, error = null, saved = null)
    }

    fun updateHolidayText(text: String) {
        _local.value = _local.value.copy(holidayText = text, error = null, saved = null)
    }

    fun save() {
        viewModelScope.launch { persist() }
    }

    private suspend fun persist() {
        val local = _local.value
        val persisted = repository.getConfig()

        val rule1 = local.rule1Text?.toIntOrNull()
        val rule2 = local.rule2Text?.toIntOrNull()
        val holiday = local.holidayText?.toIntOrNull()

        val blankOrInvalid = listOfNotNull(
            if (local.rule1Text != null && rule1 == null) "Rule 1" else null,
            if (local.rule2Text != null && rule2 == null) "Rule 2" else null,
            if (local.holidayText != null && holiday == null) "Rule 3 (Holiday)" else null
        )
        if (blankOrInvalid.isNotEmpty()) {
            _local.value = _local.value.copy(
                isSaving = false,
                error = "${blankOrInvalid.joinToString(", ")} belum diisi dengan angka yang valid - belum tersimpan."
            )
            return
        }

        _local.value = _local.value.copy(isSaving = true, error = null)
        val candidate = OvertimeRuleConfig(
            rule1PercentMin100 = rule1 ?: persisted.rule1PercentMin100,
            rule2PercentMin100 = rule2 ?: persisted.rule2PercentMin100,
            holidayPercent = holiday ?: persisted.holidayPercent
        )
        runCatching { repository.saveConfig(candidate) }
            .onSuccess { _local.value = _local.value.copy(isSaving = false, error = null, saved = "Aturan lembur disimpan.") }
            .onFailure { e -> _local.value = _local.value.copy(isSaving = false, error = e.message ?: "Gagal menyimpan.") }
    }

    fun clearMessages() {
        _local.value = _local.value.copy(error = null, saved = null)
    }
}
