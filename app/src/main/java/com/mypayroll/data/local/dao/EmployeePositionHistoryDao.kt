package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.EmployeePositionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeePositionHistoryDao {

    @Query("SELECT * FROM employee_position_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmployeePositionHistoryEntity?

    
    @Query("SELECT * FROM employee_position_history ORDER BY startDate DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<EmployeePositionHistoryEntity>>

    @Query("SELECT * FROM employee_position_history WHERE employeeId = :employeeId ORDER BY startDate DESC")
    fun observeByEmployee(employeeId: Long): kotlinx.coroutines.flow.Flow<List<EmployeePositionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmployeePositionHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<EmployeePositionHistoryEntity>): List<Long>

    @Update
    suspend fun update(entity: EmployeePositionHistoryEntity)

    @Delete
    suspend fun delete(entity: EmployeePositionHistoryEntity)
}
