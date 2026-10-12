package com.mypayroll.data.local.dao

import androidx.room.*
import com.mypayroll.data.local.entity.EmployeeContractEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeContractDao {

    @Query("SELECT * FROM employee_contract WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmployeeContractEntity?

    
    @Query("SELECT * FROM employee_contract ORDER BY startDate DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<EmployeeContractEntity>>

    @Query("SELECT * FROM employee_contract WHERE employeeId = :employeeId ORDER BY startDate DESC")
    fun observeByEmployee(employeeId: Long): kotlinx.coroutines.flow.Flow<List<EmployeeContractEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmployeeContractEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<EmployeeContractEntity>): List<Long>

    @Update
    suspend fun update(entity: EmployeeContractEntity)

    @Delete
    suspend fun delete(entity: EmployeeContractEntity)
}
