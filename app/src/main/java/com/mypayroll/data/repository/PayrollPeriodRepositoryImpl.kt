package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.PayrollPeriodDao
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import com.mypayroll.domain.repository.PayrollPeriodRepository
import kotlinx.coroutines.flow.Flow

class PayrollPeriodRepositoryImpl(
    private val dao: PayrollPeriodDao
) : PayrollPeriodRepository {
    override fun observeAll(): Flow<List<PayrollPeriodEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): PayrollPeriodEntity? = dao.getById(id)
    override suspend fun getByCode(code: String): PayrollPeriodEntity? = dao.getByCode(code)
    override suspend fun insert(entity: PayrollPeriodEntity): Long = dao.insert(entity)
    override suspend fun update(entity: PayrollPeriodEntity) = dao.update(entity)
    override suspend fun delete(entity: PayrollPeriodEntity) = dao.delete(entity)
}
