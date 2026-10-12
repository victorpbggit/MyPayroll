package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.PayrollApprovalEntity
import kotlinx.coroutines.flow.Flow

interface PayrollApprovalRepository {
    fun observeAll(): Flow<List<PayrollApprovalEntity>>
    fun observeByPeriod(payrollPeriodId: Long): Flow<List<PayrollApprovalEntity>>
    suspend fun getById(id: Long): PayrollApprovalEntity?
    suspend fun insert(entity: PayrollApprovalEntity): Long
    suspend fun delete(entity: PayrollApprovalEntity)
}
