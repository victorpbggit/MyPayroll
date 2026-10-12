package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.ContractTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractTypeDao {

    @Query("SELECT * FROM contract_type WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ContractTypeEntity?

    
    @Query("SELECT * FROM contract_type ORDER BY name")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<ContractTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ContractTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<ContractTypeEntity>): List<Long>

    @Update
    suspend fun update(entity: ContractTypeEntity)

    @Delete
    suspend fun delete(entity: ContractTypeEntity)
}
