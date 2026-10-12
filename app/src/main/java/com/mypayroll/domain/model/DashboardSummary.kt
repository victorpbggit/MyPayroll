package com.mypayroll.domain.model

import com.mypayroll.data.local.entity.EmployeeContractEntity
import java.time.LocalDate

data class RecentEvent(val date: LocalDate, val description: String)

data class DashboardSummary(
    val totalEmployees: Int = 0,
    val activeEmployees: Int = 0,
    val todayPresent: Int = 0,
    val todayLate: Int = 0,
    val todayAbsent: Int = 0,
    val todayNotRecorded: Int = 0,
    val pendingLeaveRequests: Int = 0,
    val expiringContracts: List<EmployeeContractEntity> = emptyList(),
    val pendingOvertimeAssignments: Int = 0,
    val approvedOvertimeMinutesThisMonth: Int = 0,
    val payrollPeriodLabel: String = "Belum ada periode payroll",
    val payrollPeriodStatusLabel: String = "-",
    val recentEvents: List<RecentEvent> = emptyList()
)
