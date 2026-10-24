package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.EmployeeVariableSalaryHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeVariableSalaryHistoryDao {

    @Query("SELECT * FROM employee_variable_salary_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmployeeVariableSalaryHistoryEntity?

    @Query("SELECT * FROM employee_variable_salary_history ORDER BY effectiveDate DESC")
    fun observeAll(): Flow<List<EmployeeVariableSalaryHistoryEntity>>

    @Query("SELECT * FROM employee_variable_salary_history WHERE employeeId = :employeeId ORDER BY effectiveDate DESC")
    fun observeByEmployee(employeeId: Long): Flow<List<EmployeeVariableSalaryHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmployeeVariableSalaryHistoryEntity): Long

    @Update
    suspend fun update(entity: EmployeeVariableSalaryHistoryEntity)

    @Delete
    suspend fun delete(entity: EmployeeVariableSalaryHistoryEntity)
}
