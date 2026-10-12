package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.OvertimeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OvertimeRecordDao {

    @Query("SELECT * FROM overtime_record WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): OvertimeRecordEntity?

    
    @Query("SELECT * FROM overtime_record ORDER BY id DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<OvertimeRecordEntity>>

    
    @Query("SELECT * FROM overtime_record WHERE assignmentId = :assignmentId ORDER BY id DESC")
    fun observeByAssignment(assignmentId: Long): kotlinx.coroutines.flow.Flow<List<OvertimeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: OvertimeRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<OvertimeRecordEntity>): List<Long>

    @Update
    suspend fun update(entity: OvertimeRecordEntity)

    @Delete
    suspend fun delete(entity: OvertimeRecordEntity)
}
