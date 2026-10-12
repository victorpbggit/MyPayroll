package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.PayrollPeriodEntity
import kotlinx.coroutines.flow.Flow

interface PayrollPeriodRepository {
    fun observeAll(): Flow<List<PayrollPeriodEntity>>
    suspend fun getById(id: Long): PayrollPeriodEntity?
    suspend fun getByCode(code: String): PayrollPeriodEntity?
    suspend fun insert(entity: PayrollPeriodEntity): Long
    suspend fun update(entity: PayrollPeriodEntity)
    suspend fun delete(entity: PayrollPeriodEntity)
}
