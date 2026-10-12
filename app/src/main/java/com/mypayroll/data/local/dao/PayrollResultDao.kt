package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.PayrollResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayrollResultDao {

    @Query("SELECT * FROM payroll_result WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PayrollResultEntity?

    @Query("SELECT * FROM payroll_result WHERE payrollPeriodId = :payrollPeriodId AND employeeId = :employeeId LIMIT 1")
    suspend fun getByPeriodAndEmployee(payrollPeriodId: Long, employeeId: Long): PayrollResultEntity?

    @Query("SELECT * FROM payroll_result ORDER BY calculatedAt DESC")
    fun observeAll(): Flow<List<PayrollResultEntity>>

    @Query("SELECT * FROM payroll_result WHERE payrollPeriodId = :payrollPeriodId")
    fun observeByPeriod(payrollPeriodId: Long): Flow<List<PayrollResultEntity>>

    @Query("SELECT * FROM payroll_result WHERE employeeId = :employeeId ORDER BY calculatedAt DESC")
    fun observeByEmployee(employeeId: Long): Flow<List<PayrollResultEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PayrollResultEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<PayrollResultEntity>): List<Long>

    @Update
    suspend fun update(entity: PayrollResultEntity)

    @Delete
    suspend fun delete(entity: PayrollResultEntity)
}
