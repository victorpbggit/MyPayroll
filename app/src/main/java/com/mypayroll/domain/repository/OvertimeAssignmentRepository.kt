package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.OvertimeAssignmentEntity
import kotlinx.coroutines.flow.Flow

interface OvertimeAssignmentRepository {

    fun observeAll(): Flow<List<OvertimeAssignmentEntity>>
    suspend fun getById(id: Long): OvertimeAssignmentEntity?
    suspend fun insert(entity: OvertimeAssignmentEntity): Long
    suspend fun update(entity: OvertimeAssignmentEntity)
    suspend fun delete(entity: OvertimeAssignmentEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<OvertimeAssignmentEntity>>

}
