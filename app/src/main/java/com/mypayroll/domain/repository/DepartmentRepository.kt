package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.DepartmentEntity
import kotlinx.coroutines.flow.Flow

interface DepartmentRepository {

    fun observeAll(): Flow<List<DepartmentEntity>>
    suspend fun getById(id: Long): DepartmentEntity?
    suspend fun insert(entity: DepartmentEntity): Long
    suspend fun update(entity: DepartmentEntity)
    suspend fun delete(entity: DepartmentEntity)

}
