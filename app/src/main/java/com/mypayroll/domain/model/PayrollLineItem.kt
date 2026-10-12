package com.mypayroll.domain.model

data class PayrollLineItem(
    val employeeId: Long,
    val employeeName: String,
    val basicSalary: Long,
    val totalAllowance: Long,
    val totalIncentive: Long,
    val totalOvertimeAmount: Long,
    val attendanceDeduction: Long,
    val bpjsDeduction: Long,
    val taxDeduction: Long
) {
    val totalDeduction: Long get() = attendanceDeduction + bpjsDeduction + taxDeduction
    val grossPay: Long get() = basicSalary + totalAllowance + totalIncentive + totalOvertimeAmount
    val netPay: Long get() = grossPay - totalDeduction
}
