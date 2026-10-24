package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.EmployeeVariableSalaryHistoryEntity
import kotlinx.coroutines.flow.Flow

interface EmployeeVariableSalaryHistoryRepository {

    fun observeAll(): Flow<List<EmployeeVariableSalaryHistoryEntity>>
    suspend fun getById(id: Long): EmployeeVariableSalaryHistoryEntity?
    suspend fun insert(entity: EmployeeVariableSalaryHistoryEntity): Long
    suspend fun update(entity: EmployeeVariableSalaryHistoryEntity)
    suspend fun delete(entity: EmployeeVariableSalaryHistoryEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<EmployeeVariableSalaryHistoryEntity>>

}
