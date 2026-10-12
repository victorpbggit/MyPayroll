package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.PositionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PositionDao {

    @Query("SELECT * FROM position WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PositionEntity?


    @Query("SELECT * FROM position ORDER BY name")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<PositionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PositionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<PositionEntity>): List<Long>

    @Update
    suspend fun update(entity: PositionEntity)

    @Delete
    suspend fun delete(entity: PositionEntity)
}
