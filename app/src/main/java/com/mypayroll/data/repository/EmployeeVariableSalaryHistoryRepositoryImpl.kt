package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.EmployeeVariableSalaryHistoryDao
import com.mypayroll.data.local.entity.EmployeeVariableSalaryHistoryEntity
import com.mypayroll.domain.repository.EmployeeVariableSalaryHistoryRepository
import kotlinx.coroutines.flow.Flow

class EmployeeVariableSalaryHistoryRepositoryImpl(
    private val dao: EmployeeVariableSalaryHistoryDao
) : EmployeeVariableSalaryHistoryRepository {

    override fun observeAll(): Flow<List<EmployeeVariableSalaryHistoryEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): EmployeeVariableSalaryHistoryEntity? = dao.getById(id)
    override suspend fun insert(entity: EmployeeVariableSalaryHistoryEntity): Long = dao.insert(entity)
    override suspend fun update(entity: EmployeeVariableSalaryHistoryEntity) = dao.update(entity)
    override suspend fun delete(entity: EmployeeVariableSalaryHistoryEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<EmployeeVariableSalaryHistoryEntity>> = dao.observeByEmployee(employeeId)
}
