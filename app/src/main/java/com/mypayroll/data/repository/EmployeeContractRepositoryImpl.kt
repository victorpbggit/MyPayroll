package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.EmployeeContractDao
import com.mypayroll.data.local.entity.EmployeeContractEntity
import com.mypayroll.domain.repository.EmployeeContractRepository
import kotlinx.coroutines.flow.Flow

class EmployeeContractRepositoryImpl(
    private val dao: EmployeeContractDao
) : EmployeeContractRepository {


    override fun observeAll(): Flow<List<EmployeeContractEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): EmployeeContractEntity? = dao.getById(id)
    override suspend fun insert(entity: EmployeeContractEntity): Long = dao.insert(entity)
    override suspend fun update(entity: EmployeeContractEntity) = dao.update(entity)
    override suspend fun delete(entity: EmployeeContractEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<EmployeeContractEntity>> = dao.observeByEmployee(employeeId)

}
