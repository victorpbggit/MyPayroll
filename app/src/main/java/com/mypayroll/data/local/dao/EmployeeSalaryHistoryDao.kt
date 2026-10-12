package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.EmployeeSalaryHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeSalaryHistoryDao {

    @Query("SELECT * FROM employee_salary_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmployeeSalaryHistoryEntity?

    
    @Query("SELECT * FROM employee_salary_history ORDER BY effectiveDate DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<EmployeeSalaryHistoryEntity>>

    @Query("SELECT * FROM employee_salary_history WHERE employeeId = :employeeId ORDER BY effectiveDate DESC")
    fun observeByEmployee(employeeId: Long): kotlinx.coroutines.flow.Flow<List<EmployeeSalaryHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmployeeSalaryHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<EmployeeSalaryHistoryEntity>): List<Long>

    @Update
    suspend fun update(entity: EmployeeSalaryHistoryEntity)

    @Delete
    suspend fun delete(entity: EmployeeSalaryHistoryEntity)
}
