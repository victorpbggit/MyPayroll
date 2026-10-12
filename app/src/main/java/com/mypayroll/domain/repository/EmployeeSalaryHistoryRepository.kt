package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.EmployeeSalaryHistoryEntity
import kotlinx.coroutines.flow.Flow

interface EmployeeSalaryHistoryRepository {

    fun observeAll(): Flow<List<EmployeeSalaryHistoryEntity>>
    suspend fun getById(id: Long): EmployeeSalaryHistoryEntity?
    suspend fun insert(entity: EmployeeSalaryHistoryEntity): Long
    suspend fun update(entity: EmployeeSalaryHistoryEntity)
    suspend fun delete(entity: EmployeeSalaryHistoryEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<EmployeeSalaryHistoryEntity>>

}
