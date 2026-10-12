package com.mypayroll.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "payroll_result",
    foreignKeys = [
        ForeignKey(entity = PayrollPeriodEntity::class, parentColumns = ["id"], childColumns = ["payrollPeriodId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index(value = ["payrollPeriodId", "employeeId"], unique = true), Index("employeeId")]
)
data class PayrollResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val payrollPeriodId: Long,
    val employeeId: Long,
    val basicSalary: Long,
    val totalAllowance: Long,
    val totalIncentive: Long,
    val totalOvertimeAmount: Long,
    val attendanceDeduction: Long,
    val totalDeduction: Long,
    val grossPay: Long,
    val netPay: Long,
    val status: PayrollResultStatus = PayrollResultStatus.DRAFT,
    val calculatedAt: LocalDateTime,
    val notes: String? = null
)
