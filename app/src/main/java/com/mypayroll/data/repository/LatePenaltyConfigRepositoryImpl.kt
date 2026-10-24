package com.mypayroll.data.repository

import android.content.Context
import com.mypayroll.domain.model.LatePenaltyConfig
import com.mypayroll.domain.repository.LatePenaltyConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Konfigurasi global (bukan per-karyawan/per-periode) - tidak ada Entity Room yang cocok
 * untuk satu baris nominal penalty bertingkat, jadi disimpan di SharedPreferences, sama
 * seperti repository config global lainnya. Ini TIDAK menyimpan hasil hitungan penalty
 * per karyawan - itu tetap dihitung dari data absensi asli lewat
 * [com.mypayroll.domain.payroll.LatePenaltyCalculator], bukan dari sini.
 */
class LatePenaltyConfigRepositoryImpl(context: Context) : LatePenaltyConfigRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(readConfig())

    override fun observeConfig() = _config.asStateFlow()

    override suspend fun getConfig(): LatePenaltyConfig = _config.value

    override suspend fun saveConfig(config: LatePenaltyConfig) {
        config.validate()
        prefs.edit()
            .putLong(KEY_TIER1, config.tier1AmountMin1000)
            .putLong(KEY_TIER2, config.tier2AmountMin1000)
            .putLong(KEY_TIER3, config.tier3AmountMin1000)
            .apply()
        _config.value = config
    }

    private fun readConfig(): LatePenaltyConfig = LatePenaltyConfig(
        tier1AmountMin1000 = prefs.getLong(KEY_TIER1, 1_000L),
        tier2AmountMin1000 = prefs.getLong(KEY_TIER2, 1_000L),
        tier3AmountMin1000 = prefs.getLong(KEY_TIER3, 1_000L)
    )

    private companion object {
        const val PREFS_NAME = "late_penalty_config_prefs"
        const val KEY_TIER1 = "tier1_amount"
        const val KEY_TIER2 = "tier2_amount"
        const val KEY_TIER3 = "tier3_amount"
    }
}
