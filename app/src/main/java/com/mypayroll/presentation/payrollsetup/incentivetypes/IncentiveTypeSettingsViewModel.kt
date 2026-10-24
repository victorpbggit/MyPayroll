package com.mypayroll.presentation.payrollsetup.incentivetypes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.IncentiveTypeEntity
import com.mypayroll.domain.repository.IncentiveTypeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class IncentiveTypeSettingsUiState(
    val items: List<IncentiveTypeEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

/** Katalog nama insentif global ("Pengaturan Nama Insentif" di Payroll Setup) - dipakai saat Tambah Insentif di Kompensasi. */
class IncentiveTypeSettingsViewModel(
    private val repository: IncentiveTypeRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _infoMessage = MutableStateFlow<String?>(null)

    val uiState = combine(repository.observeAll(), _errorMessage, _infoMessage) { items, error, info ->
        IncentiveTypeSettingsUiState(items = items, isLoading = false, errorMessage = error, infoMessage = info)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IncentiveTypeSettingsUiState())

    fun add(name: String, description: String?) {
        viewModelScope.launch {
            runCatching {
                require(name.isNotBlank()) { "Nama insentif tidak boleh kosong." }
                repository.insert(IncentiveTypeEntity(name = name.trim(), description = description?.trim()?.ifBlank { null }))
            }.onSuccess {
                _infoMessage.value = "Nama insentif \"$name\" berhasil disimpan."
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan nama insentif" }
        }
    }

    fun delete(item: IncentiveTypeEntity) {
        viewModelScope.launch {
            runCatching { repository.delete(item) }
                .onSuccess { _infoMessage.value = "Nama insentif \"${item.name}\" dihapus." }
                .onFailure { e -> _errorMessage.value = e.message ?: "Gagal menghapus nama insentif" }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _infoMessage.value = null
    }
}
