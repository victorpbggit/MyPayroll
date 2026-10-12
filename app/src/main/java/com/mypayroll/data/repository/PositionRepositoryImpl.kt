package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.PositionDao
import com.mypayroll.data.local.entity.PositionEntity
import com.mypayroll.domain.repository.PositionRepository
import kotlinx.coroutines.flow.Flow

class PositionRepositoryImpl(
    private val dao: PositionDao
) : PositionRepository {


    override fun observeAll(): Flow<List<PositionEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): PositionEntity? = dao.getById(id)
    override suspend fun insert(entity: PositionEntity): Long = dao.insert(entity)
    override suspend fun update(entity: PositionEntity) = dao.update(entity)
    override suspend fun delete(entity: PositionEntity) = dao.delete(entity)

}
