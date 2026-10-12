package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.AttendanceExceptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceExceptionDao {

    @Query("SELECT * FROM attendance_exception WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AttendanceExceptionEntity?

    
    @Query("SELECT * FROM attendance_exception ORDER BY date DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<AttendanceExceptionEntity>>

    @Query("SELECT * FROM attendance_exception WHERE employeeId = :employeeId ORDER BY date DESC")
    fun observeByEmployee(employeeId: Long): kotlinx.coroutines.flow.Flow<List<AttendanceExceptionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AttendanceExceptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<AttendanceExceptionEntity>): List<Long>

    @Update
    suspend fun update(entity: AttendanceExceptionEntity)

    @Delete
    suspend fun delete(entity: AttendanceExceptionEntity)
}
