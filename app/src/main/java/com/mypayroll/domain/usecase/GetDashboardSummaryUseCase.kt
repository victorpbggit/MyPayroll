package com.mypayroll.domain.usecase

import com.mypayroll.data.local.entity.ApprovalStatus
import com.mypayroll.data.local.entity.AttendanceStatus
import com.mypayroll.data.local.entity.ContractStatus
import com.mypayroll.data.local.entity.EmployeeStatus
import com.mypayroll.data.local.entity.OvertimeAssignmentStatus
import com.mypayroll.data.local.entity.OvertimeRecordStatus
import com.mypayroll.domain.model.DashboardSummary
import com.mypayroll.domain.model.RecentEvent
import com.mypayroll.domain.repository.AttendanceRepository
import com.mypayroll.domain.repository.EmployeeContractRepository
import com.mypayroll.domain.repository.EmployeeRepository
import com.mypayroll.domain.repository.LeaveRequestRepository
import com.mypayroll.domain.repository.OvertimeAssignmentRepository
import com.mypayroll.domain.repository.OvertimeRecordRepository
import com.mypayroll.domain.repository.PayrollPeriodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.YearMonth

/**
 * Aggregates dashboard statistics purely from existing data (no fabricated numbers).
 * A missing attendance record for today is reported separately as "not recorded",
 * never folded into the absent count.
 */
class GetDashboardSummaryUseCase(
    private val employeeRepository: EmployeeRepository,
    private val attendanceRepository: AttendanceRepository,
    private val leaveRequestRepository: LeaveRequestRepository,
    private val employeeContractRepository: EmployeeContractRepository,
    private val overtimeAssignmentRepository: OvertimeAssignmentRepository,
    private val overtimeRecordRepository: OvertimeRecordRepository,
    private val payrollPeriodRepository: PayrollPeriodRepository
) {
    operator fun invoke(): Flow<DashboardSummary> {
        val today = LocalDate.now()
        val thisMonth = YearMonth.from(today)

        val part1 = combine(
            employeeRepository.observeAll(),
            attendanceRepository.observeAll(),
            leaveRequestRepository.observeAll()
        ) { employees, attendance, leaveRequests ->
            val activeEmployees = employees.filter { it.status == EmployeeStatus.ACTIVE }
            val todayRecords = attendance.filter { it.workDate == today }
            val recordedIds = todayRecords.map { it.employeeId }.toSet()
            Triple(
                employees.size to activeEmployees.size,
                Triple(
                    todayRecords.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE },
                    todayRecords.count { it.status == AttendanceStatus.LATE },
                    todayRecords.count { it.status == AttendanceStatus.ABSENT || it.status == AttendanceStatus.UNEXCUSED }
                ),
                Pair(activeEmployees.count { it.id !in recordedIds }, leaveRequests.count { it.status == ApprovalStatus.PENDING })
            )
        }

        val part2 = combine(
            employeeContractRepository.observeAll(),
            overtimeAssignmentRepository.observeAll(),
            overtimeRecordRepository.observeAll()
        ) { contracts, assignments, records ->
            val expiring = contracts.filter {
                it.status == ContractStatus.ACTIVE && it.endDate != null &&
                    !it.endDate.isBefore(today) && it.endDate.isBefore(today.plusDays(31))
            }.sortedBy { it.endDate }
            val pendingAssignments = assignments.count { it.status == OvertimeAssignmentStatus.ASSIGNED }
            val approvedMinutesThisMonth = records
                .filter { it.status == OvertimeRecordStatus.APPROVED }
                .filter { record ->
                    val assignment = assignments.firstOrNull { it.id == record.assignmentId }
                    assignment != null && YearMonth.from(assignment.workDate) == thisMonth
                }
                .sumOf { it.approvedMinutes }
            Triple(expiring, pendingAssignments, approvedMinutesThisMonth)
        }

        return combine(part1, part2, payrollPeriodRepository.observeAll()) { p1, p2, periods ->
            val (employeeCounts, attendanceCounts, otherCounts) = p1
            val (expiringContracts, pendingAssignments, approvedOvertimeMinutes) = p2
            val latestPeriod = periods.maxByOrNull { it.startDate }

            val recentEvents = buildList {
                expiringContracts.take(3).forEach {
                    add(RecentEvent(it.endDate!!, "Kontrak ${it.contractNumber} akan berakhir"))
                }
            }.sortedBy { it.date }

            DashboardSummary(
                totalEmployees = employeeCounts.first,
                activeEmployees = employeeCounts.second,
                todayPresent = attendanceCounts.first,
                todayLate = attendanceCounts.second,
                todayAbsent = attendanceCounts.third,
                todayNotRecorded = otherCounts.first,
                pendingLeaveRequests = otherCounts.second,
                expiringContracts = expiringContracts,
                pendingOvertimeAssignments = pendingAssignments,
                approvedOvertimeMinutesThisMonth = approvedOvertimeMinutes,
                payrollPeriodLabel = latestPeriod?.name ?: "Belum ada periode payroll",
                payrollPeriodStatusLabel = latestPeriod?.status?.name ?: "-",
                recentEvents = recentEvents
            )
        }
    }
}
