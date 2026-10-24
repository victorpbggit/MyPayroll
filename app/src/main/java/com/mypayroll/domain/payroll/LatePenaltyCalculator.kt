package com.mypayroll.domain.payroll

import com.mypayroll.domain.model.LatePenaltyConfig

/**
 * Menghitung total penalty keterlambatan kumulatif dari menit terlambat aktual
 * (misal [com.mypayroll.data.local.entity.AttendanceEntity.lateMinutes]), bukan dari
 * data palsu/diketik ulang. Murni, tanpa ketergantungan Room/DAO/Entity.
 *
 * BELUM dipanggil oleh jalur payroll resmi (PreviewPayrollUseCase/FinalizePayrollUseCase) -
 * itu membutuhkan Entity/kolom baru untuk menyimpan nominal penalty per hasil payroll,
 * yang belum diotorisasi. Lihat mod report untuk detail blocker.
 */
object LatePenaltyCalculator {
    fun calculate(lateMinutes: Int, config: LatePenaltyConfig): Long {
        require(lateMinutes >= 0) { "Menit terlambat tidak boleh negatif." }
        var total = 0L
        if (lateMinutes >= 30) {
            total += config.tier1AmountMin1000 + config.tier2AmountMin1000 + config.tier3AmountMin1000
        } else if (lateMinutes >= 15) {
            total += config.tier1AmountMin1000 + config.tier2AmountMin1000
        } else if (lateMinutes >= 5) {
            total += config.tier1AmountMin1000
        }
        return total
    }
}
