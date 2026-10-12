package com.mypayroll.domain.usecase

import com.mypayroll.data.local.entity.PayrollApprovalAction
import com.mypayroll.data.local.entity.PayrollApprovalEntity
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import com.mypayroll.data.local.entity.PayrollPeriodStatus
import com.mypayroll.data.local.entity.PayrollResultEntity
import com.mypayroll.data.local.entity.PayrollResultStatus
import com.mypayroll.domain.model.PayrollLineItem
import com.mypayroll.domain.repository.PayrollApprovalRepository
import com.mypayroll.domain.repository.PayrollPeriodRepository
import com.mypayroll.domain.repository.PayrollResultRepository
import java.time.LocalDateTime

/**
 * Persists a computed preview as the period's payroll result snapshot. Re-running this on
 * the same period updates the existing rows for each employee instead of inserting
 * duplicates (the DB also enforces a unique (period, employee) index as a backstop).
 * Refused once the period is already FINALIZED, so a finalized payroll cannot be
 * silently changed by re-approving over it.
 */
class ApprovePayrollUseCase(
    private val payrollResultRepository: PayrollResultRepository,
    private val payrollPeriodRepository: PayrollPeriodRepository,
    private val payrollApprovalRepository: PayrollApprovalRepository
) {
    suspend operator fun invoke(period: PayrollPeriodEntity, lineItems: List<PayrollLineItem>, approverId: Long?) {
        check(period.status != PayrollPeriodStatus.FINALIZED) { "Periode sudah difinalisasi dan tidak bisa diproses ulang." }

        val now = LocalDateTime.now()
        lineItems.forEach { line ->
            val existing = payrollResultRepository.getByPeriodAndEmployee(period.id, line.employeeId)
            val entity = PayrollResultEntity(
                id = existing?.id ?: 0,
                payrollPeriodId = period.id,
                employeeId = line.employeeId,
                basicSalary = line.basicSalary,
                totalAllowance = line.totalAllowance,
                totalIncentive = line.totalIncentive,
                totalOvertimeAmount = line.totalOvertimeAmount,
                attendanceDeduction = line.attendanceDeduction,
                totalDeduction = line.totalDeduction,
                grossPay = line.grossPay,
                netPay = line.netPay,
                status = PayrollResultStatus.APPROVED,
                calculatedAt = now
            )
            if (existing != null) payrollResultRepository.update(entity) else payrollResultRepository.insert(entity)
        }

        payrollPeriodRepository.update(period.copy(status = PayrollPeriodStatus.APPROVED))
        payrollApprovalRepository.insert(
            PayrollApprovalEntity(payrollPeriodId = period.id, action = PayrollApprovalAction.APPROVED, actedBy = approverId, actedAt = now)
        )
    }
}
