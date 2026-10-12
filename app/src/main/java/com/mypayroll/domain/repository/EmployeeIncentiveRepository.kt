package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.EmployeeIncentiveEntity
import kotlinx.coroutines.flow.Flow

interface EmployeeIncentiveRepository {

    fun observeAll(): Flow<List<EmployeeIncentiveEntity>>
    suspend fun getById(id: Long): EmployeeIncentiveEntity?
    suspend fun insert(entity: EmployeeIncentiveEntity): Long
    suspend fun update(entity: EmployeeIncentiveEntity)
    suspend fun delete(entity: EmployeeIncentiveEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<EmployeeIncentiveEntity>>

}
