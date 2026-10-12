package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.EmployeeEntity
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {

    fun observeAll(): Flow<List<EmployeeEntity>>
    suspend fun getById(id: Long): EmployeeEntity?
    suspend fun insert(entity: EmployeeEntity): Long
    suspend fun update(entity: EmployeeEntity)
    suspend fun delete(entity: EmployeeEntity)
    suspend fun getByNik(nik: String): EmployeeEntity?

}
