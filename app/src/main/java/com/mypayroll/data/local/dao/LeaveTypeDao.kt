package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.LeaveTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveTypeDao {

    @Query("SELECT * FROM leave_type WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LeaveTypeEntity?


    @Query("SELECT * FROM leave_type ORDER BY name")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<LeaveTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: LeaveTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<LeaveTypeEntity>): List<Long>

    @Update
    suspend fun update(entity: LeaveTypeEntity)

    @Delete
    suspend fun delete(entity: LeaveTypeEntity)
}
