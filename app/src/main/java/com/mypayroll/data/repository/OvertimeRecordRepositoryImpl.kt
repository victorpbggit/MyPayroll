package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.OvertimeRecordDao
import com.mypayroll.data.local.entity.OvertimeRecordEntity
import com.mypayroll.domain.repository.OvertimeRecordRepository
import kotlinx.coroutines.flow.Flow

class OvertimeRecordRepositoryImpl(
    private val dao: OvertimeRecordDao
) : OvertimeRecordRepository {


    override fun observeAll(): Flow<List<OvertimeRecordEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): OvertimeRecordEntity? = dao.getById(id)
    override suspend fun insert(entity: OvertimeRecordEntity): Long = dao.insert(entity)
    override suspend fun update(entity: OvertimeRecordEntity) = dao.update(entity)
    override suspend fun delete(entity: OvertimeRecordEntity) = dao.delete(entity)
    override fun observeByAssignment(assignmentId: Long): Flow<List<OvertimeRecordEntity>> = dao.observeByAssignment(assignmentId)

}
