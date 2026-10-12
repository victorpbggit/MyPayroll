package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.PayComponentEntity
import kotlinx.coroutines.flow.Flow

interface PayComponentRepository {

    fun observeAll(): Flow<List<PayComponentEntity>>
    suspend fun getById(id: Long): PayComponentEntity?
    suspend fun insert(entity: PayComponentEntity): Long
    suspend fun update(entity: PayComponentEntity)
    suspend fun delete(entity: PayComponentEntity)

}
