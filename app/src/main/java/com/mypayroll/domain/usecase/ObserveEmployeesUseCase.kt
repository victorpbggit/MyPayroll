package com.mypayroll.domain.usecase

import com.mypayroll.domain.repository.EmployeeRepository

class ObserveEmployeesUseCase(private val repository: EmployeeRepository) {
    operator fun invoke() = repository.observeAll()
}
