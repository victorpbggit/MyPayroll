package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.EmployeePositionHistoryEntity
import kotlinx.coroutines.flow.Flow

interface EmployeePositionHistoryRepository {

    fun observeAll(): Flow<List<EmployeePositionHistoryEntity>>
    suspend fun getById(id: Long): EmployeePositionHistoryEntity?
    suspend fun insert(entity: EmployeePositionHistoryEntity): Long
    suspend fun update(entity: EmployeePositionHistoryEntity)
    suspend fun delete(entity: EmployeePositionHistoryEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<EmployeePositionHistoryEntity>>

}
