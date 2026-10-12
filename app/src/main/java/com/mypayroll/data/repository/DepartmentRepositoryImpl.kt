package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.DepartmentDao
import com.mypayroll.data.local.entity.DepartmentEntity
import com.mypayroll.domain.repository.DepartmentRepository
import kotlinx.coroutines.flow.Flow

class DepartmentRepositoryImpl(
    private val dao: DepartmentDao
) : DepartmentRepository {


    override fun observeAll(): Flow<List<DepartmentEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): DepartmentEntity? = dao.getById(id)
    override suspend fun insert(entity: DepartmentEntity): Long = dao.insert(entity)
    override suspend fun update(entity: DepartmentEntity) = dao.update(entity)
    override suspend fun delete(entity: DepartmentEntity) = dao.delete(entity)

}
