package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "attendance_exception",
    foreignKeys = [ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("employeeId", "date"), Index("documentId"), Index("approvedBy")]
)
data class AttendanceExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val date: LocalDate,
    val type: AttendanceExceptionType,
    val reason: String? = null,
    val status: ApprovalStatus = ApprovalStatus.PENDING,
    val approvedBy: Long? = null,
    val documentId: Long? = null,
    val notes: String? = null
)
