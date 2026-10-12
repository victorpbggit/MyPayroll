package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.OvertimeAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OvertimeAssignmentDao {

    @Query("SELECT * FROM overtime_assignment WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): OvertimeAssignmentEntity?


    @Query("SELECT * FROM overtime_assignment ORDER BY workDate DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<OvertimeAssignmentEntity>>

    @Query("SELECT * FROM overtime_assignment WHERE employeeId = :employeeId ORDER BY workDate DESC")
    fun observeByEmployee(employeeId: Long): kotlinx.coroutines.flow.Flow<List<OvertimeAssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: OvertimeAssignmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<OvertimeAssignmentEntity>): List<Long>

    @Update
    suspend fun update(entity: OvertimeAssignmentEntity)

    @Delete
    suspend fun delete(entity: OvertimeAssignmentEntity)
}
