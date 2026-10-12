package com.mypayroll.domain.usecase

import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.domain.repository.EmployeeRepository

class DeleteEmployeeUseCase(private val repository: EmployeeRepository) {
    suspend operator fun invoke(employee: EmployeeEntity) = repository.delete(employee)
}
