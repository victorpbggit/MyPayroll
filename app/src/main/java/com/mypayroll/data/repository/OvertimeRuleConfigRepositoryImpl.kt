package com.mypayroll.data.repository

import android.content.Context
import com.mypayroll.domain.model.OvertimeRuleConfig
import com.mypayroll.domain.repository.OvertimeRuleConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Konfigurasi global (bukan per-karyawan/per-periode) - tidak ada Entity Room yang cocok,
 * jadi disimpan di SharedPreferences, pola yang sama dipakai oleh repository config global lainnya.
 */
class OvertimeRuleConfigRepositoryImpl(context: Context) : OvertimeRuleConfigRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(readConfig())

    override fun observeConfig() = _config.asStateFlow()

    override suspend fun getConfig(): OvertimeRuleConfig = _config.value

    override suspend fun saveConfig(config: OvertimeRuleConfig) {
        config.validate()
        prefs.edit()
            .putInt(KEY_RULE1, config.rule1PercentMin100)
            .putInt(KEY_RULE2, config.rule2PercentMin100)
            .putInt(KEY_HOLIDAY, config.holidayPercent)
            .apply()
        _config.value = config
    }

    private fun readConfig(): OvertimeRuleConfig = OvertimeRuleConfig(
        rule1PercentMin100 = prefs.getInt(KEY_RULE1, 100),
        rule2PercentMin100 = prefs.getInt(KEY_RULE2, 100),
        holidayPercent = prefs.getInt(KEY_HOLIDAY, 0)
    )

    private companion object {
        const val PREFS_NAME = "overtime_rule_config_prefs"
        const val KEY_RULE1 = "rule1_percent"
        const val KEY_RULE2 = "rule2_percent"
        const val KEY_HOLIDAY = "holiday_percent"
    }
}
