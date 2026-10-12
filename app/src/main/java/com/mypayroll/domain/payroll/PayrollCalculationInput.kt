package com.mypayroll.domain.payroll

enum class EmployeePayrollType {
    MANAGER,
    NON_MANAGER
}

/** Komponen penghasilan tambahan di luar komponen payroll standar. */
data class AdditionalEarning(
    val name: String,
    val amount: Long
)

/** Komponen pengurang, misalnya penalti, peringatan, kasbon, atau potongan lain. */
data class PayrollDeduction(
    val name: String,
    val amount: Long
)

/** Satu kejadian lembur yang disetujui. PL dibayar jika durasi > 2 jam. */
data class OvertimeEvent(
    val hours: Int,
    val overtimePremiumPerEvent: Long
)

/**
 * Semua nominal uang dalam rupiah (Long).
 * Manager memakai UM/PH bulanan; non-Manager memakai tarif harian.
 */
data class PayrollCalculationInput(
    val employeeType: EmployeePayrollType,
    val basicSalaryMonthly: Long,
    val positionAllowanceMonthly: Long,

    val mealAllowanceMonthlyManager: Long = 0L,
    val attendancePremiumMonthlyManager: Long = 0L,

    val mealAllowanceDaily: Long = 0L,
    val attendancePremiumDaily: Long = 0L,

    val scheduledWorkDays: Int,
    val attendanceDays: Int,

    val overtimeHourlyRate: Long,
    val overtimeEvents: List<OvertimeEvent> = emptyList(),

    // Komponen penambah/pengurang fleksibel untuk semua tipe karyawan.
    val additionalEarnings: List<AdditionalEarning> = emptyList(),
    val deductions: List<PayrollDeduction> = emptyList()
)
