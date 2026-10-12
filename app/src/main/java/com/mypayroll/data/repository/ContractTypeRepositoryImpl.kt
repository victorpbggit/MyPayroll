package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.ContractTypeDao
import com.mypayroll.data.local.entity.ContractTypeEntity
import com.mypayroll.domain.repository.ContractTypeRepository
import kotlinx.coroutines.flow.Flow

class ContractTypeRepositoryImpl(
    private val dao: ContractTypeDao
) : ContractTypeRepository {


    override fun observeAll(): Flow<List<ContractTypeEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): ContractTypeEntity? = dao.getById(id)
    override suspend fun insert(entity: ContractTypeEntity): Long = dao.insert(entity)
    override suspend fun update(entity: ContractTypeEntity) = dao.update(entity)
    override suspend fun delete(entity: ContractTypeEntity) = dao.delete(entity)

}
