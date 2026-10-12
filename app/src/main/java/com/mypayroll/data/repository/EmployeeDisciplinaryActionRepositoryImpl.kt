package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.EmployeeDisciplinaryActionDao
import com.mypayroll.data.local.entity.EmployeeDisciplinaryActionEntity
import com.mypayroll.domain.repository.EmployeeDisciplinaryActionRepository
import kotlinx.coroutines.flow.Flow

class EmployeeDisciplinaryActionRepositoryImpl(
    private val dao: EmployeeDisciplinaryActionDao
) : EmployeeDisciplinaryActionRepository {


    override fun observeAll(): Flow<List<EmployeeDisciplinaryActionEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): EmployeeDisciplinaryActionEntity? = dao.getById(id)
    override suspend fun insert(entity: EmployeeDisciplinaryActionEntity): Long = dao.insert(entity)
    override suspend fun update(entity: EmployeeDisciplinaryActionEntity) = dao.update(entity)
    override suspend fun delete(entity: EmployeeDisciplinaryActionEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<EmployeeDisciplinaryActionEntity>> = dao.observeByEmployee(employeeId)

}
