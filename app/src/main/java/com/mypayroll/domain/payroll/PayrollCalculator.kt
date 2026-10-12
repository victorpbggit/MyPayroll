package com.mypayroll.domain.payroll

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Kalkulator payroll murni, tanpa ketergantungan Room/DAO/Entity/UI.
 *
 * Manager:
 * - GP, TJ, UM bulanan, PH bulanan dibayar penuh.
 * - Komponen pengurang berlaku jika dimasukkan, tanpa batas tipe karyawan.
 *
 * Non-Manager:
 * - GP dan TJ diprorata: hari hadir / hari kerja terjadwal.
 * - UM dan PH dihitung berdasarkan hari hadir.
 *
 * Lembur per kejadian:
 * - Jam pertama 100% UL.
 * - Jam kedua 150% UL.
 * - Jam di atas 2 jam 200% UL.
 * - PL satu kali per kejadian jika durasi > 2 jam.
 *
 * THP = total penghasilan - total komponen pengurang.
 */
class PayrollCalculator {

    fun calculate(input: PayrollCalculationInput): PayrollCalculationResult {
        validate(input)

        val isManager = input.employeeType == EmployeePayrollType.MANAGER

        val basicSalaryPaid = if (isManager) {
            input.basicSalaryMonthly
        } else {
            prorate(input.basicSalaryMonthly, input.attendanceDays, input.scheduledWorkDays)
        }

        val positionAllowancePaid = if (isManager) {
            input.positionAllowanceMonthly
        } else {
            prorate(input.positionAllowanceMonthly, input.attendanceDays, input.scheduledWorkDays)
        }

        val mealAllowancePaid = if (isManager) {
            input.mealAllowanceMonthlyManager
        } else {
            multiplyExact(input.mealAllowanceDaily, input.attendanceDays.toLong(), "Uang makan")
        }

        val attendancePremiumPaid = if (isManager) {
            input.attendancePremiumMonthlyManager
        } else {
            multiplyExact(input.attendancePremiumDaily, input.attendanceDays.toLong(), "Premi hadir")
        }

        var overtimePay = 0L
        var overtimePremium = 0L

        input.overtimeEvents.forEachIndexed { index, event ->
            val hours = event.hours

            // Jam pertama: 100% UL.
            overtimePay = addExact(
                overtimePay,
                multiplyExact(input.overtimeHourlyRate, minOf(hours, 1).toLong(), "UL jam pertama"),
                "Total UL"
            )

            // Jam kedua: 150% UL.
            val secondHour = minOf(maxOf(hours - 1, 0), 1)
            overtimePay = addExact(
                overtimePay,
                calculateOvertimeSegment(input.overtimeHourlyRate, secondHour, 150),
                "Total UL"
            )

            // Jam di atas 2: 200% UL.
            val remainingHours = maxOf(hours - 2, 0)
            overtimePay = addExact(
                overtimePay,
                calculateOvertimeSegment(input.overtimeHourlyRate, remainingHours, 200),
                "Total UL"
            )

            // PL satu kali per kejadian jika durasi > 2 jam.
            if (hours > 2) {
                overtimePremium = addExact(
                    overtimePremium,
                    event.overtimePremiumPerEvent,
                    "Total PL pada kejadian lembur ke-${index + 1}"
                )
            }
        }

        val additionalEarningsTotal = sumAmounts(
            input.additionalEarnings.map { it.amount },
            "Total komponen penambah tambahan"
        )
        val deductionsTotal = sumAmounts(
            input.deductions.map { it.amount },
            "Total komponen pengurang"
        )

        val totalEarnings = listOf(
            basicSalaryPaid,
            positionAllowancePaid,
            mealAllowancePaid,
            attendancePremiumPaid,
            overtimePay,
            overtimePremium,
            additionalEarningsTotal
        ).fold(0L) { total, amount ->
            addExact(total, amount, "Total penghasilan")
        }

        require(deductionsTotal <= totalEarnings) {
            "Total komponen pengurang tidak boleh melebihi total penghasilan."
        }

        return PayrollCalculationResult(
            basicSalaryPaid = basicSalaryPaid,
            positionAllowancePaid = positionAllowancePaid,
            mealAllowancePaid = mealAllowancePaid,
            attendancePremiumPaid = attendancePremiumPaid,
            overtimePay = overtimePay,
            overtimePremium = overtimePremium,
            additionalEarningsTotal = additionalEarningsTotal,
            deductionsTotal = deductionsTotal,
            totalEarnings = totalEarnings,
            takeHomePay = totalEarnings - deductionsTotal
        )
    }

    private fun prorate(
        monthlyAmount: Long,
        attendanceDays: Int,
        scheduledWorkDays: Int
    ): Long {
        return BigDecimal.valueOf(monthlyAmount)
            .multiply(BigDecimal.valueOf(attendanceDays.toLong()))
            .divide(
                BigDecimal.valueOf(scheduledWorkDays.toLong()),
                0,
                RoundingMode.HALF_UP
            )
            .longValueExact()
    }

    private fun calculateOvertimeSegment(
        hourlyRate: Long,
        hours: Int,
        percentage: Int
    ): Long {
        return BigDecimal.valueOf(hourlyRate)
            .multiply(BigDecimal.valueOf(hours.toLong()))
            .multiply(BigDecimal.valueOf(percentage.toLong()))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
            .longValueExact()
    }

    private fun multiplyExact(amount: Long, quantity: Long, label: String): Long {
        return try {
            Math.multiplyExact(amount, quantity)
        } catch (_: ArithmeticException) {
            throw IllegalArgumentException("$label melebihi batas perhitungan.")
        }
    }

    private fun addExact(first: Long, second: Long, label: String): Long {
        return try {
            Math.addExact(first, second)
        } catch (_: ArithmeticException) {
            throw IllegalArgumentException("$label melebihi batas perhitungan.")
        }
    }

    private fun sumAmounts(amounts: List<Long>, label: String): Long {
        require(amounts.all { it >= 0L }) {
            "$label tidak boleh berisi nominal negatif."
        }
        return amounts.fold(0L) { total, amount ->
            addExact(total, amount, label)
        }
    }

    private fun validate(input: PayrollCalculationInput) {
        require(input.scheduledWorkDays > 0) {
            "Hari kerja terjadwal harus lebih dari 0."
        }
        require(input.attendanceDays in 0..input.scheduledWorkDays) {
            "Hari hadir harus antara 0 dan hari kerja terjadwal."
        }

        val amounts = listOf(
            input.basicSalaryMonthly,
            input.positionAllowanceMonthly,
            input.mealAllowanceMonthlyManager,
            input.attendancePremiumMonthlyManager,
            input.mealAllowanceDaily,
            input.attendancePremiumDaily,
            input.overtimeHourlyRate
        )
        require(amounts.all { it >= 0L }) {
            "Nominal payroll tidak boleh negatif."
        }

        require(input.overtimeEvents.all {
            it.hours >= 0 && it.overtimePremiumPerEvent >= 0L
        }) {
            "Jam lembur dan premi lembur tidak boleh negatif."
        }

        require(input.additionalEarnings.all {
            it.name.isNotBlank() && it.amount >= 0L
        }) {
            "Komponen penambah harus memiliki nama dan nominal tidak negatif."
        }

        require(input.deductions.all {
            it.name.isNotBlank() && it.amount >= 0L
        }) {
            "Komponen pengurang harus memiliki nama dan nominal tidak negatif."
        }
    }
}
