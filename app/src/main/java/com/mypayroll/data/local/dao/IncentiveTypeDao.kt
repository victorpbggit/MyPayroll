package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.IncentiveTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncentiveTypeDao {

    @Query("SELECT * FROM incentive_type WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): IncentiveTypeEntity?

    @Query("SELECT * FROM incentive_type ORDER BY name ASC")
    fun observeAll(): Flow<List<IncentiveTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: IncentiveTypeEntity): Long

    @Update
    suspend fun update(entity: IncentiveTypeEntity)

    @Delete
    suspend fun delete(entity: IncentiveTypeEntity)
}
