package com.mypayroll.domain.payroll

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Menurunkan GP Per Hari (GPPH) dan GP Per Jam (GPPJ) dari Gaji Pokok (GP) bulanan dan
 * pengaturan umum (days per month/week, hours per week) di Payroll Setup. Murni tampilan untuk
 * tab Gaji Pokok pada Kompensasi - TIDAK dipakai oleh [PayrollCalculator]/[CalculatePayrollUseCase]
 * atau jalur payroll resmi mana pun, dan tidak mengubah formula payroll yang sudah ada.
 *
 * Pembulatan HALF_UP ke Rupiah bulat terdekat, tidak pernah pecahan desimal.
 */
private fun divideWholeRupiah(amount: Long, divisor: Int): Long {
    if (divisor <= 0) return 0L
    return BigDecimal.valueOf(amount)
        .divide(BigDecimal.valueOf(divisor.toLong()), 0, RoundingMode.HALF_UP)
        .longValueExact()
}

fun gajiPokokPerHari(gajiPokokBulanan: Long, daysPerMonth: Int): Long = divideWholeRupiah(gajiPokokBulanan, daysPerMonth)

fun gajiPokokPerJam(gajiPokokPerHari: Long, daysPerWeek: Int, hoursPerWeek: Int): Long {
    if (hoursPerWeek <= 0) return 0L
    return BigDecimal.valueOf(gajiPokokPerHari)
        .multiply(BigDecimal.valueOf(daysPerWeek.toLong()))
        .divide(BigDecimal.valueOf(hoursPerWeek.toLong()), 0, RoundingMode.HALF_UP)
        .longValueExact()
}

/**
 * Membagi [total] menjadi 3 bagian proporsional (UM/UT/UH) tanpa pecahan desimal - sisa
 * pembagian (jika ada) didistribusikan 1 Rupiah ke bagian pertama secara berurutan, sehingga
 * um + ut + uh selalu sama persis dengan [total].
 */
fun splitVariableSalaryEqually(total: Long): Triple<Long, Long, Long> {
    val base = total / 3
    val remainder = (total % 3).toInt()
    val um = base + if (remainder >= 1) 1 else 0
    val ut = base + if (remainder >= 2) 1 else 0
    val uh = base
    return Triple(um, ut, uh)
}

/**
 * Gaji Variable (GV) bulanan dibagi proporsional ke UM/UT/UH ([splitVariableSalaryEqually]),
 * lalu masing-masing bagian dibagi lagi dengan [daysPerMonth] (DM) untuk mendapatkan nominal
 * UM/UT/UH PER HARI - tetap tanpa pecahan desimal (HALF_UP ke Rupiah bulat terdekat di langkah
 * kedua). Ini yang disimpan sebagai nilai UM/UT/UH di riwayat Gaji Variable.
 */
fun splitVariableSalaryDaily(totalMonthly: Long, daysPerMonth: Int): Triple<Long, Long, Long> {
    val (umMonthly, utMonthly, uhMonthly) = splitVariableSalaryEqually(totalMonthly)
    return Triple(
        divideWholeRupiah(umMonthly, daysPerMonth),
        divideWholeRupiah(utMonthly, daysPerMonth),
        divideWholeRupiah(uhMonthly, daysPerMonth)
    )
}
