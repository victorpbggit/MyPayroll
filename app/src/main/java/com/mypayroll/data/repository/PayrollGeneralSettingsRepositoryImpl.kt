package com.mypayroll.data.repository

import android.content.Context
import com.mypayroll.domain.model.PayrollGeneralSettings
import com.mypayroll.domain.repository.PayrollGeneralSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Konfigurasi global (bukan per-karyawan/per-periode) - tidak ada Entity Room yang cocok untuk
 * satu baris pengaturan umum (simbol mata uang, days per month/week, hours per week), jadi
 * disimpan di SharedPreferences, pola yang sama dengan repository config global lainnya
 * (Calendar/Overtime Rules/Late Penalty).
 */
class PayrollGeneralSettingsRepositoryImpl(context: Context) : PayrollGeneralSettingsRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(readSettings())

    override fun observeSettings() = _settings.asStateFlow()

    override suspend fun getSettings(): PayrollGeneralSettings = _settings.value

    override suspend fun saveSettings(settings: PayrollGeneralSettings) {
        settings.validate()
        prefs.edit()
            .putString(KEY_CURRENCY, settings.currency)
            .putInt(KEY_DAYS_PER_MONTH, settings.daysPerMonth)
            .putInt(KEY_DAYS_PER_WEEK, settings.daysPerWeek)
            .putInt(KEY_HOURS_PER_WEEK, settings.hoursPerWeek)
            .apply()
        _settings.value = settings
    }

    private fun readSettings(): PayrollGeneralSettings = PayrollGeneralSettings(
        currency = prefs.getString(KEY_CURRENCY, null) ?: "Rp",
        daysPerMonth = prefs.getInt(KEY_DAYS_PER_MONTH, 25),
        daysPerWeek = prefs.getInt(KEY_DAYS_PER_WEEK, 6),
        hoursPerWeek = prefs.getInt(KEY_HOURS_PER_WEEK, 40)
    )

    private companion object {
        const val PREFS_NAME = "payroll_general_settings_prefs"
        const val KEY_CURRENCY = "currency"
        const val KEY_DAYS_PER_MONTH = "days_per_month"
        const val KEY_DAYS_PER_WEEK = "days_per_week"
        const val KEY_HOURS_PER_WEEK = "hours_per_week"
    }
}
