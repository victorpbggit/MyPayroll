package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.PayrollApprovalDao
import com.mypayroll.data.local.entity.PayrollApprovalEntity
import com.mypayroll.domain.repository.PayrollApprovalRepository
import kotlinx.coroutines.flow.Flow

class PayrollApprovalRepositoryImpl(
    private val dao: PayrollApprovalDao
) : PayrollApprovalRepository {
    override fun observeAll(): Flow<List<PayrollApprovalEntity>> = dao.observeAll()
    override fun observeByPeriod(payrollPeriodId: Long): Flow<List<PayrollApprovalEntity>> = dao.observeByPeriod(payrollPeriodId)
    override suspend fun getById(id: Long): PayrollApprovalEntity? = dao.getById(id)
    override suspend fun insert(entity: PayrollApprovalEntity): Long = dao.insert(entity)
    override suspend fun delete(entity: PayrollApprovalEntity) = dao.delete(entity)
}
