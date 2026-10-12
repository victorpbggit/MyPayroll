package com.mypayroll.domain.model

/**
 * Configurable payroll policy inputs. None of these are hardcoded "official" Indonesian
 * tax/BPJS/overtime rules - this project has not had those confirmed, so every rate
 * defaults to zero (no effect) and must be entered explicitly per payroll run.
 * Overtime pay itself is NOT configured here: it comes straight from each approved
 * [com.mypayroll.data.local.entity.OvertimeRecordEntity.calculationAmount], which is
 * already a Rupiah amount entered when the overtime was recorded.
 */
data class PayrollPolicy(
    val lateDeductionPerMinute: Long = 0,
    val absentDeductionPerDay: Long = 0,
    val bpjsEmployeeRatePercent: Double = 0.0,
    val incomeTaxRatePercent: Double = 0.0
) {
    companion object {
        /** Human-readable note shown in the UI next to the policy inputs. */
        const val DISCLAIMER =
            "Tarif BPJS, PPh21, dan potongan keterlambatan/absen di bawah ini BELUM diverifikasi sebagai " +
                "aturan resmi perusahaan. Nilai default adalah 0 (tidak ada potongan). Isi sesuai kebijakan " +
                "yang sudah dikonfirmasi sebelum menggunakan hasil ini sebagai dasar pembayaran."
    }
}
