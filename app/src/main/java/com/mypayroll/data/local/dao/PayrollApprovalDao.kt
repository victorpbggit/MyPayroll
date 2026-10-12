package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.PayrollApprovalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayrollApprovalDao {

    @Query("SELECT * FROM payroll_approval WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PayrollApprovalEntity?

    @Query("SELECT * FROM payroll_approval ORDER BY actedAt DESC")
    fun observeAll(): Flow<List<PayrollApprovalEntity>>

    @Query("SELECT * FROM payroll_approval WHERE payrollPeriodId = :payrollPeriodId ORDER BY actedAt DESC")
    fun observeByPeriod(payrollPeriodId: Long): Flow<List<PayrollApprovalEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PayrollApprovalEntity): Long

    @Delete
    suspend fun delete(entity: PayrollApprovalEntity)
}
