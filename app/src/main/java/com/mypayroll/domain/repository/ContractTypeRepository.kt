package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.ContractTypeEntity
import kotlinx.coroutines.flow.Flow

interface ContractTypeRepository {

    fun observeAll(): Flow<List<ContractTypeEntity>>
    suspend fun getById(id: Long): ContractTypeEntity?
    suspend fun insert(entity: ContractTypeEntity): Long
    suspend fun update(entity: ContractTypeEntity)
    suspend fun delete(entity: ContractTypeEntity)

}
