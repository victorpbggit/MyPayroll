package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.EmployeePayComponentHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeePayComponentHistoryDao {

    @Query("SELECT * FROM employee_pay_component_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmployeePayComponentHistoryEntity?

    
    @Query("SELECT * FROM employee_pay_component_history ORDER BY effectiveDate DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<EmployeePayComponentHistoryEntity>>

    @Query("SELECT * FROM employee_pay_component_history WHERE employeeId = :employeeId ORDER BY effectiveDate DESC")
    fun observeByEmployee(employeeId: Long): kotlinx.coroutines.flow.Flow<List<EmployeePayComponentHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmployeePayComponentHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<EmployeePayComponentHistoryEntity>): List<Long>

    @Update
    suspend fun update(entity: EmployeePayComponentHistoryEntity)

    @Delete
    suspend fun delete(entity: EmployeePayComponentHistoryEntity)
}
