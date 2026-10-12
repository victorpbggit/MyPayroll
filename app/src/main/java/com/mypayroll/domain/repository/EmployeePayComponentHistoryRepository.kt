package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.EmployeePayComponentHistoryEntity
import kotlinx.coroutines.flow.Flow

interface EmployeePayComponentHistoryRepository {

    fun observeAll(): Flow<List<EmployeePayComponentHistoryEntity>>
    suspend fun getById(id: Long): EmployeePayComponentHistoryEntity?
    suspend fun insert(entity: EmployeePayComponentHistoryEntity): Long
    suspend fun update(entity: EmployeePayComponentHistoryEntity)
    suspend fun delete(entity: EmployeePayComponentHistoryEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<EmployeePayComponentHistoryEntity>>

}
