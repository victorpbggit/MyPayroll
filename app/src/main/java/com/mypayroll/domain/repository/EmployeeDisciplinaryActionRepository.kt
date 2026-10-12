package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.EmployeeDisciplinaryActionEntity
import kotlinx.coroutines.flow.Flow

interface EmployeeDisciplinaryActionRepository {

    fun observeAll(): Flow<List<EmployeeDisciplinaryActionEntity>>
    suspend fun getById(id: Long): EmployeeDisciplinaryActionEntity?
    suspend fun insert(entity: EmployeeDisciplinaryActionEntity): Long
    suspend fun update(entity: EmployeeDisciplinaryActionEntity)
    suspend fun delete(entity: EmployeeDisciplinaryActionEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<EmployeeDisciplinaryActionEntity>>

}
