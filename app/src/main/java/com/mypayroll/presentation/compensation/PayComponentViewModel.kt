package com.mypayroll.presentation.compensation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.CalculationType
import com.mypayroll.data.local.entity.PayComponentCategory
import com.mypayroll.data.local.entity.PayComponentEntity
import com.mypayroll.domain.repository.PayComponentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PayComponentUiState(
    val items: List<PayComponentEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class PayComponentViewModel(private val repository: PayComponentRepository) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PayComponentUiState> = combine(
        repository.observeAll().catch { e ->
            _errorMessage.value = e.message ?: "Gagal membaca data"
            emit(emptyList())
        },
        _errorMessage
    ) { items, error ->
        PayComponentUiState(items = items.sortedBy { it.name }, isLoading = false, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PayComponentUiState())

    fun add(
        code: String,
        name: String,
        category: PayComponentCategory,
        calculationType: CalculationType,
        taxable: Boolean,
        bpjsApplicable: Boolean
    ) {
        viewModelScope.launch {
            runCatching {
                repository.insert(
                    PayComponentEntity(
                        code = code,
                        name = name,
                        category = category,
                        calculationType = calculationType,
                        taxable = taxable,
                        bpjsApplicable = bpjsApplicable
                    )
                )
            }.onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan komponen gaji" }
        }
    }

    fun toggleActive(item: PayComponentEntity) {
        viewModelScope.launch {
            runCatching { repository.update(item.copy(active = !item.active)) }
                .onFailure { e -> _errorMessage.value = e.message ?: "Gagal memperbarui data" }
        }
    }

    fun remove(item: PayComponentEntity) {
        viewModelScope.launch {
            runCatching { repository.delete(item) }
                .onFailure { e -> _errorMessage.value = e.message ?: "Gagal menghapus data" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
