package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.PayrollResultEntity
import kotlinx.coroutines.flow.Flow

interface PayrollResultRepository {
    fun observeAll(): Flow<List<PayrollResultEntity>>
    fun observeByPeriod(payrollPeriodId: Long): Flow<List<PayrollResultEntity>>
    fun observeByEmployee(employeeId: Long): Flow<List<PayrollResultEntity>>
    suspend fun getById(id: Long): PayrollResultEntity?
    suspend fun getByPeriodAndEmployee(payrollPeriodId: Long, employeeId: Long): PayrollResultEntity?
    suspend fun insert(entity: PayrollResultEntity): Long
    suspend fun insertAll(entities: List<PayrollResultEntity>): List<Long>
    suspend fun update(entity: PayrollResultEntity)
    suspend fun delete(entity: PayrollResultEntity)
}
