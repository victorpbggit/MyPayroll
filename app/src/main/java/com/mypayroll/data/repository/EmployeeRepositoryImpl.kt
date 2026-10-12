package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.EmployeeDao
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.Flow

class EmployeeRepositoryImpl(
    private val dao: EmployeeDao
) : EmployeeRepository {


    override fun observeAll(): Flow<List<EmployeeEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): EmployeeEntity? = dao.getById(id)
    override suspend fun insert(entity: EmployeeEntity): Long = dao.insert(entity)
    override suspend fun update(entity: EmployeeEntity) = dao.update(entity)
    override suspend fun delete(entity: EmployeeEntity) = dao.delete(entity)
    override suspend fun getByNik(nik: String): EmployeeEntity? = dao.getByNik(nik)

}
