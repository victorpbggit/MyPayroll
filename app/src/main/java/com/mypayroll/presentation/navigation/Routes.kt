package com.mypayroll.presentation.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val EMPLOYEES = "employees"
    const val EMPLOYEE_DETAIL = "employees/{employeeId}"
    const val EMPLOYEE_EDIT = "employees/{employeeId}/edit"
    const val ATTENDANCE = "attendance"
    const val LEAVE = "leave"
    const val OVERTIME = "overtime"
    const val ORGANIZATION = "organization"
    const val DOCUMENTS = "documents"
    const val MORE = "more"
    const val PAYROLL = "payroll"
    const val PAYROLL_DETAIL = "payroll/{periodId}"
    const val PAYROLL_CALCULATOR = "payroll/{periodId}/calculator"
    const val PAYROLL_SETUP = "payroll/setup"

    fun employeeDetail(id: Long) = "employees/$id"
    fun employeeEdit(id: Long) = "employees/$id/edit"
    fun payrollDetail(id: Long) = "payroll/$id"
    fun payrollCalculator(id: Long) = "payroll/$id/calculator"
}
