package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.LeaveRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveRequestDao {

    @Query("SELECT * FROM leave_request WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LeaveRequestEntity?


    @Query("SELECT * FROM leave_request ORDER BY startDate DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_request WHERE employeeId = :employeeId ORDER BY startDate DESC")
    fun observeByEmployee(employeeId: Long): kotlinx.coroutines.flow.Flow<List<LeaveRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: LeaveRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<LeaveRequestEntity>): List<Long>

    @Update
    suspend fun update(entity: LeaveRequestEntity)

    @Delete
    suspend fun delete(entity: LeaveRequestEntity)
}
