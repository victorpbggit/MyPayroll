package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.IncentiveTypeEntity
import kotlinx.coroutines.flow.Flow

interface IncentiveTypeRepository {
    fun observeAll(): Flow<List<IncentiveTypeEntity>>
    suspend fun getById(id: Long): IncentiveTypeEntity?
    suspend fun insert(entity: IncentiveTypeEntity): Long
    suspend fun update(entity: IncentiveTypeEntity)
    suspend fun delete(entity: IncentiveTypeEntity)
}
