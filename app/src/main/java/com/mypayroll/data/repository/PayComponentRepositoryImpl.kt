package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.PayComponentDao
import com.mypayroll.data.local.entity.PayComponentEntity
import com.mypayroll.domain.repository.PayComponentRepository
import kotlinx.coroutines.flow.Flow

class PayComponentRepositoryImpl(
    private val dao: PayComponentDao
) : PayComponentRepository {


    override fun observeAll(): Flow<List<PayComponentEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): PayComponentEntity? = dao.getById(id)
    override suspend fun insert(entity: PayComponentEntity): Long = dao.insert(entity)
    override suspend fun update(entity: PayComponentEntity) = dao.update(entity)
    override suspend fun delete(entity: PayComponentEntity) = dao.delete(entity)

}
