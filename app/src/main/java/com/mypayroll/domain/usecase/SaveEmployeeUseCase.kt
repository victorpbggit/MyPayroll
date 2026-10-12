package com.mypayroll.domain.usecase

import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.domain.repository.EmployeeRepository

class SaveEmployeeUseCase(private val repository: EmployeeRepository) {
    suspend operator fun invoke(employee: EmployeeEntity) {
        if (employee.id == 0L) {
            repository.insert(employee)
        } else {
            repository.update(employee)
        }
    }
}
