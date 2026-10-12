package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM document WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DocumentEntity?

    
    @Query("SELECT * FROM document ORDER BY createdAt DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: DocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<DocumentEntity>): List<Long>

    @Update
    suspend fun update(entity: DocumentEntity)

    @Delete
    suspend fun delete(entity: DocumentEntity)
}
