package com.mypayroll.domain.model

/**
 * Persentase UL (Upah Lembur) untuk Aturan Perhitungan Lembur:
 * - [rule1PercentMin100]: lembur >= 1 jam, minimum 100.
 * - [rule2PercentMin100]: lembur >= 2 jam, minimum 100.
 * - [holidayPercent]: lembur pada tanggal berstatus Holiday di Calendar Payroll.
 *
 * CATATAN PENTING: nilai di sini TERSIMPAN tetapi BELUM dipakai oleh
 * [com.mypayroll.domain.payroll.PayrollCalculator]/[com.mypayroll.domain.payroll.CalculatePayrollUseCase].
 * Formula resmi (UL/Jam = persentase x GP/Jam, pembagi GP/Jam, dan hubungannya dengan
 * PayrollPolicy.lateDeductionPerMinute) belum dikonfirmasi - lihat mod report untuk detail
 * blocker. Jangan menganggap field ini sudah memengaruhi perhitungan payroll resmi.
 */
data class OvertimeRuleConfig(
    val rule1PercentMin100: Int = 100,
    val rule2PercentMin100: Int = 100,
    /** 0 = belum dikonfigurasi. Tidak ada minimum yang didokumentasikan untuk nilai ini (beda dari Rule 1/2). */
    val holidayPercent: Int = 0
) {
    /** Dipanggil oleh repository sebelum menyimpan - lapisan bisnis, bukan hanya validasi UI. */
    fun validate() {
        require(rule1PercentMin100 >= 100) { "Persentase Rule 1 tidak boleh kurang dari 100%." }
        require(rule2PercentMin100 >= 100) { "Persentase Rule 2 tidak boleh kurang dari 100%." }
        require(holidayPercent >= 0) { "Persentase Holiday tidak boleh negatif." }
    }
}
