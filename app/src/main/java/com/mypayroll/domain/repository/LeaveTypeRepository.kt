package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.LeaveTypeEntity
import kotlinx.coroutines.flow.Flow

interface LeaveTypeRepository {

    fun observeAll(): Flow<List<LeaveTypeEntity>>
    suspend fun getById(id: Long): LeaveTypeEntity?
    suspend fun insert(entity: LeaveTypeEntity): Long
    suspend fun update(entity: LeaveTypeEntity)
    suspend fun delete(entity: LeaveTypeEntity)

}
