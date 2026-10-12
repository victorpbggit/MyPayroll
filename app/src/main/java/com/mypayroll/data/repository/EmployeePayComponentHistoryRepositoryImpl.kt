package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.EmployeePayComponentHistoryDao
import com.mypayroll.data.local.entity.EmployeePayComponentHistoryEntity
import com.mypayroll.domain.repository.EmployeePayComponentHistoryRepository
import kotlinx.coroutines.flow.Flow

class EmployeePayComponentHistoryRepositoryImpl(
    private val dao: EmployeePayComponentHistoryDao
) : EmployeePayComponentHistoryRepository {


    override fun observeAll(): Flow<List<EmployeePayComponentHistoryEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): EmployeePayComponentHistoryEntity? = dao.getById(id)
    override suspend fun insert(entity: EmployeePayComponentHistoryEntity): Long = dao.insert(entity)
    override suspend fun update(entity: EmployeePayComponentHistoryEntity) = dao.update(entity)
    override suspend fun delete(entity: EmployeePayComponentHistoryEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<EmployeePayComponentHistoryEntity>> = dao.observeByEmployee(employeeId)

}
