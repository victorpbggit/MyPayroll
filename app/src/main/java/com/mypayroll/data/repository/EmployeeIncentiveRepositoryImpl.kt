package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.EmployeeIncentiveDao
import com.mypayroll.data.local.entity.EmployeeIncentiveEntity
import com.mypayroll.domain.repository.EmployeeIncentiveRepository
import kotlinx.coroutines.flow.Flow

class EmployeeIncentiveRepositoryImpl(
    private val dao: EmployeeIncentiveDao
) : EmployeeIncentiveRepository {


    override fun observeAll(): Flow<List<EmployeeIncentiveEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): EmployeeIncentiveEntity? = dao.getById(id)
    override suspend fun insert(entity: EmployeeIncentiveEntity): Long = dao.insert(entity)
    override suspend fun update(entity: EmployeeIncentiveEntity) = dao.update(entity)
    override suspend fun delete(entity: EmployeeIncentiveEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<EmployeeIncentiveEntity>> = dao.observeByEmployee(employeeId)

}
