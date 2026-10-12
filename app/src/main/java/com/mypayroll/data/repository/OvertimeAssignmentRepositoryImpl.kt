package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.OvertimeAssignmentDao
import com.mypayroll.data.local.entity.OvertimeAssignmentEntity
import com.mypayroll.domain.repository.OvertimeAssignmentRepository
import kotlinx.coroutines.flow.Flow

class OvertimeAssignmentRepositoryImpl(
    private val dao: OvertimeAssignmentDao
) : OvertimeAssignmentRepository {


    override fun observeAll(): Flow<List<OvertimeAssignmentEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): OvertimeAssignmentEntity? = dao.getById(id)
    override suspend fun insert(entity: OvertimeAssignmentEntity): Long = dao.insert(entity)
    override suspend fun update(entity: OvertimeAssignmentEntity) = dao.update(entity)
    override suspend fun delete(entity: OvertimeAssignmentEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<OvertimeAssignmentEntity>> = dao.observeByEmployee(employeeId)

}
