package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.PayrollResultDao
import com.mypayroll.data.local.entity.PayrollResultEntity
import com.mypayroll.domain.repository.PayrollResultRepository
import kotlinx.coroutines.flow.Flow

class PayrollResultRepositoryImpl(
    private val dao: PayrollResultDao
) : PayrollResultRepository {
    override fun observeAll(): Flow<List<PayrollResultEntity>> = dao.observeAll()
    override fun observeByPeriod(payrollPeriodId: Long): Flow<List<PayrollResultEntity>> = dao.observeByPeriod(payrollPeriodId)
    override fun observeByEmployee(employeeId: Long): Flow<List<PayrollResultEntity>> = dao.observeByEmployee(employeeId)
    override suspend fun getById(id: Long): PayrollResultEntity? = dao.getById(id)
    override suspend fun getByPeriodAndEmployee(payrollPeriodId: Long, employeeId: Long): PayrollResultEntity? =
        dao.getByPeriodAndEmployee(payrollPeriodId, employeeId)
    override suspend fun insert(entity: PayrollResultEntity): Long = dao.insert(entity)
    override suspend fun insertAll(entities: List<PayrollResultEntity>): List<Long> = dao.insertAll(entities)
    override suspend fun update(entity: PayrollResultEntity) = dao.update(entity)
    override suspend fun delete(entity: PayrollResultEntity) = dao.delete(entity)
}
