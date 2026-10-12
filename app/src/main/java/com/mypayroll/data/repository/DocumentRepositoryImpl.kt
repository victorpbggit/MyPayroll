package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.DocumentDao
import com.mypayroll.data.local.entity.DocumentEntity
import com.mypayroll.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

class DocumentRepositoryImpl(
    private val dao: DocumentDao
) : DocumentRepository {


    override fun observeAll(): Flow<List<DocumentEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): DocumentEntity? = dao.getById(id)
    override suspend fun insert(entity: DocumentEntity): Long = dao.insert(entity)
    override suspend fun update(entity: DocumentEntity) = dao.update(entity)
    override suspend fun delete(entity: DocumentEntity) = dao.delete(entity)

}
