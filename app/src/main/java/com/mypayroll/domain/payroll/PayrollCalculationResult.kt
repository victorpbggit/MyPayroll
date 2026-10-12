package com.mypayroll.domain.payroll

data class PayrollCalculationResult(
    val basicSalaryPaid: Long,
    val positionAllowancePaid: Long,
    val mealAllowancePaid: Long,
    val attendancePremiumPaid: Long,
    val overtimePay: Long,
    val overtimePremium: Long,

    val additionalEarningsTotal: Long,
    val deductionsTotal: Long,
    val totalEarnings: Long,
    val takeHomePay: Long
)
