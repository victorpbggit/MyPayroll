package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.DepartmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentDao {

    @Query("SELECT * FROM department WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DepartmentEntity?

    
    @Query("SELECT * FROM department ORDER BY name")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<DepartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: DepartmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<DepartmentEntity>): List<Long>

    @Update
    suspend fun update(entity: DepartmentEntity)

    @Delete
    suspend fun delete(entity: DepartmentEntity)
}
