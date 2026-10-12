package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.LeaveRequestDao
import com.mypayroll.data.local.entity.LeaveRequestEntity
import com.mypayroll.domain.repository.LeaveRequestRepository
import kotlinx.coroutines.flow.Flow

class LeaveRequestRepositoryImpl(
    private val dao: LeaveRequestDao
) : LeaveRequestRepository {


    override fun observeAll(): Flow<List<LeaveRequestEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): LeaveRequestEntity? = dao.getById(id)
    override suspend fun insert(entity: LeaveRequestEntity): Long = dao.insert(entity)
    override suspend fun update(entity: LeaveRequestEntity) = dao.update(entity)
    override suspend fun delete(entity: LeaveRequestEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<LeaveRequestEntity>> = dao.observeByEmployee(employeeId)

}
