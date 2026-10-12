package com.mypayroll.domain.repository

import com.mypayroll.data.local.entity.AttendanceEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {

    fun observeAll(): Flow<List<AttendanceEntity>>
    suspend fun getById(id: Long): AttendanceEntity?
    suspend fun insert(entity: AttendanceEntity): Long
    suspend fun update(entity: AttendanceEntity)
    suspend fun delete(entity: AttendanceEntity)
    fun observeByEmployee(employeeId: Long): Flow<List<AttendanceEntity>>
    suspend fun getByEmployeeAndDate(employeeId: Long, date: LocalDate): AttendanceEntity?

}
