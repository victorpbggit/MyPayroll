package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.PositionEntity
import kotlinx.coroutines.flow.Flow

interface PositionRepository {

    fun observeAll(): Flow<List<PositionEntity>>
    suspend fun getById(id: Long): PositionEntity?
    suspend fun insert(entity: PositionEntity): Long
    suspend fun update(entity: PositionEntity)
    suspend fun delete(entity: PositionEntity)

}
