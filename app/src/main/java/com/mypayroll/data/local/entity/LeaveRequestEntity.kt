package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "leave_request",
    foreignKeys = [
        ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = LeaveTypeEntity::class, parentColumns = ["id"], childColumns = ["leaveTypeId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("employeeId", "startDate"), Index("leaveTypeId"), Index("documentId"), Index("approvedBy")]
)
data class LeaveRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val leaveTypeId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val requestedAt: LocalDateTime,
    val approvedAt: LocalDateTime? = null,
    val approvedBy: Long? = null,
    val status: ApprovalStatus = ApprovalStatus.PENDING,
    val reason: String? = null,
    val documentId: Long? = null
)
