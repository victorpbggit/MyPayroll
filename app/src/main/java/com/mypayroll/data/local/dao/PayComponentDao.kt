package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.PayComponentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayComponentDao {

    @Query("SELECT * FROM pay_component WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PayComponentEntity?


    @Query("SELECT * FROM pay_component ORDER BY name")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<PayComponentEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PayComponentEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<PayComponentEntity>): List<Long>

    @Update
    suspend fun update(entity: PayComponentEntity)

    @Delete
    suspend fun delete(entity: PayComponentEntity)
}
