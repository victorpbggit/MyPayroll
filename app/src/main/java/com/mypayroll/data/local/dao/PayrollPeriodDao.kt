package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayrollPeriodDao {

    @Query("SELECT * FROM payroll_period WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PayrollPeriodEntity?

    @Query("SELECT * FROM payroll_period WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): PayrollPeriodEntity?

    @Query("SELECT * FROM payroll_period ORDER BY startDate DESC")
    fun observeAll(): Flow<List<PayrollPeriodEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PayrollPeriodEntity): Long

    @Update
    suspend fun update(entity: PayrollPeriodEntity)

    @Delete
    suspend fun delete(entity: PayrollPeriodEntity)
}
