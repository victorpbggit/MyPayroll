package com.mypayroll.domain.model

/**
 * Pengaturan global yang dipakai untuk menurunkan nilai GP Per Hari (GPPH) dan GP Per Jam (GPPJ)
 * di tab Gaji Pokok pada Kompensasi:
 * - GPPH = GP / [daysPerMonth]
 * - GPPJ = (GPPH * [daysPerWeek]) / [hoursPerWeek]
 *
 * [currency] adalah label simbol mata uang yang ditampilkan (bukan pengali/konversi nilai).
 */
data class PayrollGeneralSettings(
    val currency: String = "Rp",
    val daysPerMonth: Int = 25,
    val daysPerWeek: Int = 6,
    val hoursPerWeek: Int = 40
) {
    fun validate() {
        require(currency.isNotBlank()) { "Simbol mata uang tidak boleh kosong." }
        require(daysPerMonth > 0) { "Days per month harus lebih dari 0." }
        require(daysPerWeek > 0) { "Days per week harus lebih dari 0." }
        require(hoursPerWeek > 0) { "Hours per week harus lebih dari 0." }
    }
}
