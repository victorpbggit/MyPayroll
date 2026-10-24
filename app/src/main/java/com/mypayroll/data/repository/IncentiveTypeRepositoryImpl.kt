package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.IncentiveTypeDao
import com.mypayroll.data.local.entity.IncentiveTypeEntity
import com.mypayroll.domain.repository.IncentiveTypeRepository
import kotlinx.coroutines.flow.Flow

class IncentiveTypeRepositoryImpl(
    private val dao: IncentiveTypeDao
) : IncentiveTypeRepository {
    override fun observeAll(): Flow<List<IncentiveTypeEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): IncentiveTypeEntity? = dao.getById(id)
    override suspend fun insert(entity: IncentiveTypeEntity): Long = dao.insert(entity)
    override suspend fun update(entity: IncentiveTypeEntity) = dao.update(entity)
    override suspend fun delete(entity: IncentiveTypeEntity) = dao.delete(entity)
}
