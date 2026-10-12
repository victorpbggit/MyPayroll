package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(
    tableName = "overtime_record",
    foreignKeys = [ForeignKey(entity = OvertimeAssignmentEntity::class, parentColumns = ["id"], childColumns = ["assignmentId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("assignmentId")]
)
data class OvertimeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assignmentId: Long,
    val actualStart: LocalTime,
    val actualEnd: LocalTime,
    val approvedMinutes: Int = 0,
    val calculationAmount: Long = 0,
    val approvedBy: Long? = null,
    val status: OvertimeRecordStatus = OvertimeRecordStatus.PENDING
)
