package com.mypayroll.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "payroll_approval",
    foreignKeys = [ForeignKey(entity = PayrollPeriodEntity::class, parentColumns = ["id"], childColumns = ["payrollPeriodId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("payrollPeriodId")]
)
data class PayrollApprovalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val payrollPeriodId: Long,
    val action: PayrollApprovalAction,
    val actedBy: Long? = null,
    val actedAt: LocalDateTime,
    val notes: String? = null
)
