package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.EmployeePositionHistoryDao
import com.mypayroll.data.local.entity.EmployeePositionHistoryEntity
import com.mypayroll.domain.repository.EmployeePositionHistoryRepository
import kotlinx.coroutines.flow.Flow

class EmployeePositionHistoryRepositoryImpl(
    private val dao: EmployeePositionHistoryDao
) : EmployeePositionHistoryRepository {


    override fun observeAll(): Flow<List<EmployeePositionHistoryEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): EmployeePositionHistoryEntity? = dao.getById(id)
    override suspend fun insert(entity: EmployeePositionHistoryEntity): Long = dao.insert(entity)
    override suspend fun update(entity: EmployeePositionHistoryEntity) = dao.update(entity)
    override suspend fun delete(entity: EmployeePositionHistoryEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<EmployeePositionHistoryEntity>> = dao.observeByEmployee(employeeId)

}
