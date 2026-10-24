package com.mypayroll.presentation.payrollsetup.latepenalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.domain.model.LatePenaltyConfig
import com.mypayroll.domain.repository.LatePenaltyConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LatePenaltyUiState(
    val tier1Text: String = "",
    val tier2Text: String = "",
    val tier3Text: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * Draft text per field, kept as the literal string the user is typing (including "" while
 * backspacing) - never reconstructed from a parsed Long. Reconstructing from Long was a past
 * bug: an empty/invalid string failed to parse, so the update was dropped and the field froze
 * at its last valid single digit instead of going blank.
 */
private data class LocalState(
    val tier1Text: String? = null,
    val tier2Text: String? = null,
    val tier3Text: String? = null,
    val error: String? = null,
    val saved: String? = null,
    val isSaving: Boolean = false
)

/**
 * Validasi minimum Rp1.000 dijalankan di [LatePenaltyConfigRepository.saveConfig] (lapisan
 * bisnis), bukan hanya UI. Simpan HANYA lewat tombol "Simpan" (tidak ada autosave - dicabut
 * atas permintaan user).
 */
class LatePenaltyViewModel(
    private val repository: LatePenaltyConfigRepository
) : ViewModel() {

    private val _local = MutableStateFlow(LocalState())

    val uiState = combine(repository.observeConfig(), _local) { persisted, local ->
        LatePenaltyUiState(
            tier1Text = local.tier1Text ?: persisted.tier1AmountMin1000.toString(),
            tier2Text = local.tier2Text ?: persisted.tier2AmountMin1000.toString(),
            tier3Text = local.tier3Text ?: persisted.tier3AmountMin1000.toString(),
            isLoading = false,
            isSaving = local.isSaving,
            savedMessage = local.saved,
            errorMessage = local.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LatePenaltyUiState())

    fun updateTier1Text(text: String) {
        _local.value = _local.value.copy(tier1Text = text, error = null, saved = null)
    }

    fun updateTier2Text(text: String) {
        _local.value = _local.value.copy(tier2Text = text, error = null, saved = null)
    }

    fun updateTier3Text(text: String) {
        _local.value = _local.value.copy(tier3Text = text, error = null, saved = null)
    }

    fun save() {
        viewModelScope.launch { persist() }
    }

    private suspend fun persist() {
        val local = _local.value
        val persisted = repository.getConfig()

        val tier1 = local.tier1Text?.toLongOrNull()
        val tier2 = local.tier2Text?.toLongOrNull()
        val tier3 = local.tier3Text?.toLongOrNull()

        val blankOrInvalid = listOfNotNull(
            if (local.tier1Text != null && tier1 == null) "Rule 1" else null,
            if (local.tier2Text != null && tier2 == null) "Rule 2" else null,
            if (local.tier3Text != null && tier3 == null) "Rule 3" else null
        )
        if (blankOrInvalid.isNotEmpty()) {
            _local.value = _local.value.copy(
                isSaving = false,
                error = "${blankOrInvalid.joinToString(", ")} belum diisi dengan nominal yang valid - belum tersimpan."
            )
            return
        }

        _local.value = _local.value.copy(isSaving = true, error = null)
        val candidate = LatePenaltyConfig(
            tier1AmountMin1000 = tier1 ?: persisted.tier1AmountMin1000,
            tier2AmountMin1000 = tier2 ?: persisted.tier2AmountMin1000,
            tier3AmountMin1000 = tier3 ?: persisted.tier3AmountMin1000
        )
        runCatching { repository.saveConfig(candidate) }
            .onSuccess { _local.value = _local.value.copy(isSaving = false, error = null, saved = "Penalty keterlambatan disimpan.") }
            .onFailure { e -> _local.value = _local.value.copy(isSaving = false, error = e.message ?: "Gagal menyimpan.") }
    }

    fun clearMessages() {
        _local.value = _local.value.copy(error = null, saved = null)
    }
}
