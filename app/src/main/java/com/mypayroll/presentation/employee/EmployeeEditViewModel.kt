package com.mypayroll.presentation.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmployeeEditUiState(
    val employee: EmployeeEntity? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null
)

class EmployeeEditViewModel(
    private val employeeId: Long,
    private val repository: EmployeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val employee = repository.getById(employeeId)
            _uiState.value = _uiState.value.copy(employee = employee, isLoading = false)
        }
    }

    fun save(updated: EmployeeEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            runCatching { repository.update(updated) }
                .onSuccess { _uiState.value = _uiState.value.copy(isSaving = false, saved = true) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message ?: "Gagal menyimpan data") }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
