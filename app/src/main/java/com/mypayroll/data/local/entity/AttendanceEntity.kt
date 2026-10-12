package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "attendance",
    foreignKeys = [ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["employeeId", "workDate"], unique = true)]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val workDate: LocalDate,
    val scheduledIn: LocalTime? = null,
    val actualIn: LocalTime? = null,
    val scheduledOut: LocalTime? = null,
    val actualOut: LocalTime? = null,
    val status: AttendanceStatus,
    val lateMinutes: Int = 0,
    val earlyLeaveMinutes: Int = 0,
    val notes: String? = null
)
