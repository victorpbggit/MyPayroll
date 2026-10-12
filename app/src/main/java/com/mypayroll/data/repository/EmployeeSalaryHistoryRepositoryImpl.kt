package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.EmployeeSalaryHistoryDao
import com.mypayroll.data.local.entity.EmployeeSalaryHistoryEntity
import com.mypayroll.domain.repository.EmployeeSalaryHistoryRepository
import kotlinx.coroutines.flow.Flow

class EmployeeSalaryHistoryRepositoryImpl(
    private val dao: EmployeeSalaryHistoryDao
) : EmployeeSalaryHistoryRepository {


    override fun observeAll(): Flow<List<EmployeeSalaryHistoryEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): EmployeeSalaryHistoryEntity? = dao.getById(id)
    override suspend fun insert(entity: EmployeeSalaryHistoryEntity): Long = dao.insert(entity)
    override suspend fun update(entity: EmployeeSalaryHistoryEntity) = dao.update(entity)
    override suspend fun delete(entity: EmployeeSalaryHistoryEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<EmployeeSalaryHistoryEntity>> = dao.observeByEmployee(employeeId)

}
