package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(
    tableName = "overtime_assignment",
    foreignKeys = [ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("employeeId", "workDate"), Index("workOrderId"), Index("taskId")]
)
data class OvertimeAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val workDate: LocalDate,
    val plannedStart: LocalTime,
    val plannedEnd: LocalTime,
    val workOrderId: Long? = null,
    val taskId: Long? = null,
    val description: String? = null,
    val assignedBy: Long? = null,
    val assignedAt: LocalDateTime,
    val status: OvertimeAssignmentStatus = OvertimeAssignmentStatus.ASSIGNED,
    val rejectedAt: LocalDateTime? = null,
    val rejectionReason: String? = null
)
