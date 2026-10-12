package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.EmployeeIncentiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeIncentiveDao {

    @Query("SELECT * FROM employee_incentive WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmployeeIncentiveEntity?

    
    @Query("SELECT * FROM employee_incentive ORDER BY date DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<EmployeeIncentiveEntity>>

    @Query("SELECT * FROM employee_incentive WHERE employeeId = :employeeId ORDER BY date DESC")
    fun observeByEmployee(employeeId: Long): kotlinx.coroutines.flow.Flow<List<EmployeeIncentiveEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmployeeIncentiveEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<EmployeeIncentiveEntity>): List<Long>

    @Update
    suspend fun update(entity: EmployeeIncentiveEntity)

    @Delete
    suspend fun delete(entity: EmployeeIncentiveEntity)
}
