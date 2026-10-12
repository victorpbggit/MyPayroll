package com.mypayroll.presentation.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.domain.usecase.DeleteEmployeeUseCase
import com.mypayroll.domain.usecase.ObserveEmployeesUseCase
import com.mypayroll.domain.usecase.SaveEmployeeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EmployeeViewModel(
    private val observeEmployeesUseCase: ObserveEmployeesUseCase,
    private val saveEmployeeUseCase: SaveEmployeeUseCase,
    private val deleteEmployeeUseCase: DeleteEmployeeUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val employeesFlow = observeEmployeesUseCase()
        .catch { e -> _errorMessage.value = e.message ?: "Gagal membaca data"; emit(emptyList()) }

    val uiState = combine(employeesFlow, _query, _errorMessage) { employees, query, error ->
        val filtered = if (query.isBlank()) employees else employees.filter {
            it.name.contains(query, ignoreCase = true) || it.nik.contains(query, ignoreCase = true)
        }
        EmployeeUiState(employees = filtered.sortedBy { it.name }, query = query, isLoading = false, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EmployeeUiState())

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun save(employee: EmployeeEntity) {
        viewModelScope.launch {
            runCatching { saveEmployeeUseCase(employee) }
                .onFailure { e -> _errorMessage.value = e.message ?: "Gagal menyimpan data" }
        }
    }

    fun delete(employee: EmployeeEntity) {
        viewModelScope.launch {
            runCatching { deleteEmployeeUseCase(employee) }
                .onFailure { e -> _errorMessage.value = e.message ?: "Gagal menghapus data" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
