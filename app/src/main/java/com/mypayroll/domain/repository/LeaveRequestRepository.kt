package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.LeaveRequestEntity
import kotlinx.coroutines.flow.Flow

interface LeaveRequestRepository {

    fun observeAll(): Flow<List<LeaveRequestEntity>>
    suspend fun getById(id: Long): LeaveRequestEntity?
    suspend fun insert(entity: LeaveRequestEntity): Long
    suspend fun update(entity: LeaveRequestEntity)
    suspend fun delete(entity: LeaveRequestEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<LeaveRequestEntity>>

}
