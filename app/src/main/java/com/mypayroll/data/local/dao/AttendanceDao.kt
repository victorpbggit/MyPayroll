package com.mypayroll.data.local.dao
import androidx.room.*
import com.mypayroll.data.local.entity.AttendanceEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AttendanceEntity?

    
    @Query("SELECT * FROM attendance ORDER BY workDate DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId ORDER BY workDate DESC")
    fun observeByEmployee(employeeId: Long): kotlinx.coroutines.flow.Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId AND workDate = :date LIMIT 1")
    suspend fun getByEmployeeAndDate(employeeId: Long, date: LocalDate): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<AttendanceEntity>): List<Long>

    @Update
    suspend fun update(entity: AttendanceEntity)

    @Delete
    suspend fun delete(entity: AttendanceEntity)
}
