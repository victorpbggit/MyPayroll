package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {

    fun observeAll(): Flow<List<DocumentEntity>>
    suspend fun getById(id: Long): DocumentEntity?
    suspend fun insert(entity: DocumentEntity): Long
    suspend fun update(entity: DocumentEntity)
    suspend fun delete(entity: DocumentEntity)

}
