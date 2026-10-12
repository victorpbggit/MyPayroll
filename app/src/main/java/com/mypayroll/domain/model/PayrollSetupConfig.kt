package com.mypayroll.domain.model

/**
 * Nilai default komponen payroll (GP, TJ, UM, PH, UL, PL) yang diatur sekali di Payroll Setup
 * dan dipakai sebagai nilai awal saat menghitung payroll per karyawan di Kalkulator Payroll.
 * Bukan nilai final - tetap bisa diubah lagi per karyawan/periode di kalkulator.
 */
data class PayrollSetupConfig(
    val basicSalaryMonthly: Long = 0,
    val positionAllowanceMonthly: Long = 0,
    val mealAllowanceMonthlyManager: Long = 0,
    val mealAllowanceDaily: Long = 0,
    val attendancePremiumMonthlyManager: Long = 0,
    val attendancePremiumDaily: Long = 0,
    val overtimeHourlyRate: Long = 0,
    val overtimePremiumPerEvent: Long = 0
)
