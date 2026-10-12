package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.EmployeeDisciplinaryActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDisciplinaryActionDao {

    @Query("SELECT * FROM employee_disciplinary_action WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmployeeDisciplinaryActionEntity?

    
    @Query("SELECT * FROM employee_disciplinary_action ORDER BY incidentDate DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<EmployeeDisciplinaryActionEntity>>

    @Query("SELECT * FROM employee_disciplinary_action WHERE employeeId = :employeeId ORDER BY incidentDate DESC")
    fun observeByEmployee(employeeId: Long): kotlinx.coroutines.flow.Flow<List<EmployeeDisciplinaryActionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmployeeDisciplinaryActionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<EmployeeDisciplinaryActionEntity>): List<Long>

    @Update
    suspend fun update(entity: EmployeeDisciplinaryActionEntity)

    @Delete
    suspend fun delete(entity: EmployeeDisciplinaryActionEntity)
}
