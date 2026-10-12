package com.mypayroll.domain.usecase

import com.mypayroll.data.local.entity.PayrollApprovalAction
import com.mypayroll.data.local.entity.PayrollApprovalEntity
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import com.mypayroll.data.local.entity.PayrollPeriodStatus
import com.mypayroll.data.local.entity.PayrollResultStatus
import com.mypayroll.domain.repository.PayrollApprovalRepository
import com.mypayroll.domain.repository.PayrollPeriodRepository
import com.mypayroll.domain.repository.PayrollResultRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

/**
 * Locks a period's already-approved results. After this, [ApprovePayrollUseCase] refuses
 * to touch the period again, so a later change to an employee's salary/components cannot
 * silently alter a payroll that was already finalized.
 */
class FinalizePayrollUseCase(
    private val payrollResultRepository: PayrollResultRepository,
    private val payrollPeriodRepository: PayrollPeriodRepository,
    private val payrollApprovalRepository: PayrollApprovalRepository
) {
    suspend operator fun invoke(period: PayrollPeriodEntity, finalizedBy: Long?) {
        check(period.status == PayrollPeriodStatus.APPROVED) { "Periode harus disetujui (approve) terlebih dahulu sebelum difinalisasi." }

        val now = LocalDateTime.now()
        val results = payrollResultRepository.observeByPeriod(period.id).first()
        results.forEach { result -> payrollResultRepository.update(result.copy(status = PayrollResultStatus.FINALIZED)) }

        payrollPeriodRepository.update(
            period.copy(status = PayrollPeriodStatus.FINALIZED, finalizedAt = now, finalizedBy = finalizedBy)
        )
        payrollApprovalRepository.insert(
            PayrollApprovalEntity(payrollPeriodId = period.id, action = PayrollApprovalAction.FINALIZED, actedBy = finalizedBy, actedAt = now)
        )
    }
}
