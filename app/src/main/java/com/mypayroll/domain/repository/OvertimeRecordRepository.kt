package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.OvertimeRecordEntity
import kotlinx.coroutines.flow.Flow

interface OvertimeRecordRepository {

    fun observeAll(): Flow<List<OvertimeRecordEntity>>
    suspend fun getById(id: Long): OvertimeRecordEntity?
    suspend fun insert(entity: OvertimeRecordEntity): Long
    suspend fun update(entity: OvertimeRecordEntity)
    suspend fun delete(entity: OvertimeRecordEntity)
    fun observeByAssignment(assignmentId: Long): Flow<List<OvertimeRecordEntity>>

}
