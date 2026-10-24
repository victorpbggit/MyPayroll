package com.mypayroll.domain.model

/**
 * Nominal penalty keterlambatan bertingkat (kumulatif), dalam rupiah (Long, bukan floating-point).
 * Rule 1: keterlambatan >= 5 menit. Rule 2: >= 15 menit. Rule 3: >= 30 menit.
 * Ketiganya kumulatif - lihat [com.mypayroll.domain.payroll.LatePenaltyCalculator].
 */
data class LatePenaltyConfig(
    val tier1AmountMin1000: Long = 1_000L,
    val tier2AmountMin1000: Long = 1_000L,
    val tier3AmountMin1000: Long = 1_000L
) {
    /** Dipanggil oleh repository sebelum menyimpan - lapisan bisnis, bukan hanya validasi UI. */
    fun validate() {
        require(tier1AmountMin1000 >= 1_000L) { "Nominal Rule 1 tidak boleh kurang dari Rp1.000." }
        require(tier2AmountMin1000 >= 1_000L) { "Nominal Rule 2 tidak boleh kurang dari Rp1.000." }
        require(tier3AmountMin1000 >= 1_000L) { "Nominal Rule 3 tidak boleh kurang dari Rp1.000." }
    }
}
