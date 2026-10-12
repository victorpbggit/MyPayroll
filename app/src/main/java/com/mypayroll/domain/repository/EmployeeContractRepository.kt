package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.EmployeeContractEntity
import kotlinx.coroutines.flow.Flow

interface EmployeeContractRepository {

    fun observeAll(): Flow<List<EmployeeContractEntity>>
    suspend fun getById(id: Long): EmployeeContractEntity?
    suspend fun insert(entity: EmployeeContractEntity): Long
    suspend fun update(entity: EmployeeContractEntity)
    suspend fun delete(entity: EmployeeContractEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<EmployeeContractEntity>>

}
