package com.mypayroll.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "payroll_period", indices = [Index(value = ["code"], unique = true)])
data class PayrollPeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: PayrollPeriodStatus = PayrollPeriodStatus.OPEN,
    val createdAt: LocalDateTime,
    val finalizedAt: LocalDateTime? = null,
    val finalizedBy: Long? = null,
    val notes: String? = null
)
