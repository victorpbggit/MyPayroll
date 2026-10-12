package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.AttendanceExceptionDao
import com.mypayroll.data.local.entity.AttendanceExceptionEntity
import com.mypayroll.domain.repository.AttendanceExceptionRepository
import kotlinx.coroutines.flow.Flow

class AttendanceExceptionRepositoryImpl(
    private val dao: AttendanceExceptionDao
) : AttendanceExceptionRepository {


    override fun observeAll(): Flow<List<AttendanceExceptionEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): AttendanceExceptionEntity? = dao.getById(id)
    override suspend fun insert(entity: AttendanceExceptionEntity): Long = dao.insert(entity)
    override suspend fun update(entity: AttendanceExceptionEntity) = dao.update(entity)
    override suspend fun delete(entity: AttendanceExceptionEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<AttendanceExceptionEntity>> = dao.observeByEmployee(employeeId)

}
