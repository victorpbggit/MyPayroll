package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    @Query("SELECT * FROM employee WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmployeeEntity?

    @Query("SELECT * FROM employee ORDER BY name")
    fun observeAll(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employee WHERE nik = :nik LIMIT 1")
    suspend fun getByNik(nik: String): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmployeeEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<EmployeeEntity>): List<Long>

    @Update
    suspend fun update(entity: EmployeeEntity)

    @Delete
    suspend fun delete(entity: EmployeeEntity)
}
