package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.AttendanceExceptionEntity
import kotlinx.coroutines.flow.Flow

interface AttendanceExceptionRepository {

    fun observeAll(): Flow<List<AttendanceExceptionEntity>>
    suspend fun getById(id: Long): AttendanceExceptionEntity?
    suspend fun insert(entity: AttendanceExceptionEntity): Long
    suspend fun update(entity: AttendanceExceptionEntity)
    suspend fun delete(entity: AttendanceExceptionEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<AttendanceExceptionEntity>>

}
