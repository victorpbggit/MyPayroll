package com.mypayroll.domain.usecase

import com.mypayroll.data.local.entity.AttendanceStatus
import com.mypayroll.data.local.entity.EmployeeStatus
import com.mypayroll.data.local.entity.OvertimeRecordStatus
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import com.mypayroll.domain.model.PayrollLineItem
import com.mypayroll.domain.model.PayrollPolicy
import com.mypayroll.domain.repository.AttendanceRepository
import com.mypayroll.domain.repository.EmployeeIncentiveRepository
import com.mypayroll.domain.repository.EmployeeRepository
import com.mypayroll.domain.repository.EmployeeSalaryHistoryRepository
import com.mypayroll.domain.repository.OvertimeAssignmentRepository
import com.mypayroll.domain.repository.OvertimeRecordRepository
import kotlinx.coroutines.flow.first

/**
 * Computes a payroll preview for a period from existing data only: latest basic salary
 * effective on/before the period, incentives dated within the period, and approved overtime
 * whose assignment date falls within the period. Nothing here is persisted - see
 * [ApprovePayrollUseCase] / [FinalizePayrollUseCase] for that, which is a separate, explicit step.
 *
 * `totalAllowance` is always 0 as of this change: its previous source, per-employee pay-component
 * history (`EmployeePayComponentHistoryRepository`/`PayComponentEntity`), was removed at the user's
 * explicit request (see mod report). No replacement source was specified, so no formula was
 * invented here - this is a real change to official payroll output, not a bug.
 */
class PreviewPayrollUseCase(
    private val employeeRepository: EmployeeRepository,
    private val salaryHistoryRepository: EmployeeSalaryHistoryRepository,
    private val incentiveRepository: EmployeeIncentiveRepository,
    private val overtimeAssignmentRepository: OvertimeAssignmentRepository,
    private val overtimeRecordRepository: OvertimeRecordRepository,
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(period: PayrollPeriodEntity, policy: PayrollPolicy): List<PayrollLineItem> {
        val employees = employeeRepository.observeAll().first().filter { it.status == EmployeeStatus.ACTIVE }
        val allSalaryHistory = salaryHistoryRepository.observeAll().first()
        val allIncentives = incentiveRepository.observeAll().first()
        val allAssignments = overtimeAssignmentRepository.observeAll().first()
        val allRecords = overtimeRecordRepository.observeAll().first()
        val allAttendance = attendanceRepository.observeAll().first()

        return employees.map { employee ->
            val basicSalary = allSalaryHistory
                .filter { it.employeeId == employee.id && !it.effectiveDate.isAfter(period.endDate) }
                .maxByOrNull { it.effectiveDate }?.basicSalary ?: 0L

            val totalAllowance = 0L

            val totalIncentive = allIncentives
                .filter { it.employeeId == employee.id && !it.date.isBefore(period.startDate) && !it.date.isAfter(period.endDate) }
                .sumOf { it.amount }

            val employeeAssignmentIds = allAssignments.filter { it.employeeId == employee.id }.map { it.id }.toSet()
            val totalOvertimeAmount = allRecords
                .filter { it.assignmentId in employeeAssignmentIds && it.status == OvertimeRecordStatus.APPROVED }
                .filter { record ->
                    val assignment = allAssignments.first { it.id == record.assignmentId }
                    !assignment.workDate.isBefore(period.startDate) && !assignment.workDate.isAfter(period.endDate)
                }
                .sumOf { it.calculationAmount }

            val periodAttendance = allAttendance.filter {
                it.employeeId == employee.id && !it.workDate.isBefore(period.startDate) && !it.workDate.isAfter(period.endDate)
            }
            val lateMinutes = periodAttendance.sumOf { it.lateMinutes }
            val absentDays = periodAttendance.count { it.status == AttendanceStatus.ABSENT }
            val attendanceDeduction = lateMinutes * policy.lateDeductionPerMinute + absentDays * policy.absentDeductionPerDay

            val grossBeforeStatutory = basicSalary + totalAllowance + totalIncentive + totalOvertimeAmount
            val bpjsDeduction = (grossBeforeStatutory * policy.bpjsEmployeeRatePercent / 100.0).toLong()
            val taxDeduction = (grossBeforeStatutory * policy.incomeTaxRatePercent / 100.0).toLong()

            PayrollLineItem(
                employeeId = employee.id,
                employeeName = employee.name,
                basicSalary = basicSalary,
                totalAllowance = totalAllowance,
                totalIncentive = totalIncentive,
                totalOvertimeAmount = totalOvertimeAmount,
                attendanceDeduction = attendanceDeduction,
                bpjsDeduction = bpjsDeduction,
                taxDeduction = taxDeduction
            )
        }
    }
}
