package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.LeaveTypeDao
import com.mypayroll.data.local.entity.LeaveTypeEntity
import com.mypayroll.domain.repository.LeaveTypeRepository
import kotlinx.coroutines.flow.Flow

class LeaveTypeRepositoryImpl(
    private val dao: LeaveTypeDao
) : LeaveTypeRepository {


    override fun observeAll(): Flow<List<LeaveTypeEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): LeaveTypeEntity? = dao.getById(id)
    override suspend fun insert(entity: LeaveTypeEntity): Long = dao.insert(entity)
    override suspend fun update(entity: LeaveTypeEntity) = dao.update(entity)
    override suspend fun delete(entity: LeaveTypeEntity) = dao.delete(entity)

}
