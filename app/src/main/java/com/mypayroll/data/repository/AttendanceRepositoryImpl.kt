package com.mypayroll.data.repository

import com.mypayroll.data.local.dao.AttendanceDao
import com.mypayroll.data.local.entity.AttendanceEntity
import com.mypayroll.domain.repository.AttendanceRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class AttendanceRepositoryImpl(
    private val dao: AttendanceDao
) : AttendanceRepository {


    override fun observeAll(): Flow<List<AttendanceEntity>> = dao.observeAll()
    override suspend fun getById(id: Long): AttendanceEntity? = dao.getById(id)
    override suspend fun insert(entity: AttendanceEntity): Long = dao.insert(entity)
    override suspend fun update(entity: AttendanceEntity) = dao.update(entity)
    override suspend fun delete(entity: AttendanceEntity) = dao.delete(entity)
    override fun observeByEmployee(employeeId: Long): Flow<List<AttendanceEntity>> = dao.observeByEmployee(employeeId)
    override suspend fun getByEmployeeAndDate(employeeId: Long, date: LocalDate): AttendanceEntity? = dao.getByEmployeeAndDate(employeeId, date)

}
