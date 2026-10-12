package com.mypayroll.presentation.employee

import com.mypayroll.data.local.entity.EmployeeEntity

data class EmployeeUiState(
    val employees: List<EmployeeEntity> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
