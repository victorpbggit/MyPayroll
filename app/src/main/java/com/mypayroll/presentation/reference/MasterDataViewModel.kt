package com.mypayroll.presentation.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MasterDataItem<T>(
    val raw: T,
    val id: Long,
    val code: String,
    val name: String,
    val active: Boolean?
)

data class MasterDataUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val errorMessage: String? = null
)

/**
 * Generic CRUD glue for the simple "code + name (+active)" master data tables:
 * Department, Position, ContractType, LeaveType. Each entity has its own repository
 * (no shared interface), so the screen passes plain lambdas that adapt its entity
 * shape instead of this class depending on any single repository type.
 */
class MasterDataViewModel<T>(
    observeAll: () -> Flow<List<T>>,
    private val idOf: (T) -> Long,
    private val codeOf: (T) -> String,
    private val nameOf: (T) -> String,
    private val activeOf: (T) -> Boolean?,
    private val create: (code: String, name: String) -> T,
    private val withActive: (T, Boolean) -> T,
    private val insert: suspend (T) -> Long,
    private val update: suspend (T) -> Unit,
    private val delete: suspend (T) -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(MasterDataUiState())
    val uiState = _uiState.asStateFlow()

    private val itemsFlow: StateFlow<List<T>> = observeAll()
        .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Gagal membaca data") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items: StateFlow<List<MasterDataItem<T>>> = combine(itemsFlow, uiState) { list, state ->
        val filtered = if (state.query.isBlank()) list else list.filter {
            codeOf(it).contains(state.query, ignoreCase = true) || nameOf(it).contains(state.query, ignoreCase = true)
        }
        filtered.sortedBy { nameOf(it) }.map { MasterDataItem(it, idOf(it), codeOf(it), nameOf(it), activeOf(it)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun add(code: String, name: String) {
        viewModelScope.launch {
            runCatching { insert(create(code, name)) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Gagal menyimpan data") }
        }
    }

    fun toggleActive(item: MasterDataItem<T>) {
        val current = item.active ?: return
        viewModelScope.launch {
            runCatching { update(withActive(item.raw, !current)) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Gagal memperbarui data") }
        }
    }

    fun remove(item: MasterDataItem<T>) {
        viewModelScope.launch {
            runCatching { delete(item.raw) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Gagal menghapus data") }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
