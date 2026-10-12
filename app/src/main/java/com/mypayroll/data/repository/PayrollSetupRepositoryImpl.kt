package com.mypayroll.data.repository

import android.content.Context
import com.mypayroll.domain.model.PayrollSetupConfig
import com.mypayroll.domain.repository.PayrollSetupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Payroll Setup adalah satu baris konfigurasi global (bukan data per-karyawan/per-periode),
 * jadi tidak memakai Room (PayComponentEntity hanya mendefinisikan jenis komponen, dan
 * EmployeePayComponentHistoryEntity khusus riwayat per karyawan - tidak ada tempat untuk nilai
 * default tunggal seperti ini tanpa menambah Entity/DAO baru). Disimpan di SharedPreferences
 * agar package data.local.dao dan data.local.entity tidak perlu diubah.
 */
class PayrollSetupRepositoryImpl(context: Context) : PayrollSetupRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(readConfig())

    override fun observeConfig() = _config.asStateFlow()

    override suspend fun getConfig(): PayrollSetupConfig = _config.value

    override suspend fun saveConfig(config: PayrollSetupConfig) {
        prefs.edit()
            .putLong(KEY_BASIC_SALARY, config.basicSalaryMonthly)
            .putLong(KEY_POSITION_ALLOWANCE, config.positionAllowanceMonthly)
            .putLong(KEY_MEAL_MANAGER, config.mealAllowanceMonthlyManager)
            .putLong(KEY_MEAL_DAILY, config.mealAllowanceDaily)
            .putLong(KEY_ATTENDANCE_MANAGER, config.attendancePremiumMonthlyManager)
            .putLong(KEY_ATTENDANCE_DAILY, config.attendancePremiumDaily)
            .putLong(KEY_OVERTIME_RATE, config.overtimeHourlyRate)
            .putLong(KEY_OVERTIME_PREMIUM, config.overtimePremiumPerEvent)
            .apply()
        _config.value = config
    }

    private fun readConfig(): PayrollSetupConfig = PayrollSetupConfig(
        basicSalaryMonthly = prefs.getLong(KEY_BASIC_SALARY, 0L),
        positionAllowanceMonthly = prefs.getLong(KEY_POSITION_ALLOWANCE, 0L),
        mealAllowanceMonthlyManager = prefs.getLong(KEY_MEAL_MANAGER, 0L),
        mealAllowanceDaily = prefs.getLong(KEY_MEAL_DAILY, 0L),
        attendancePremiumMonthlyManager = prefs.getLong(KEY_ATTENDANCE_MANAGER, 0L),
        attendancePremiumDaily = prefs.getLong(KEY_ATTENDANCE_DAILY, 0L),
        overtimeHourlyRate = prefs.getLong(KEY_OVERTIME_RATE, 0L),
        overtimePremiumPerEvent = prefs.getLong(KEY_OVERTIME_PREMIUM, 0L)
    )

    private companion object {
        const val PREFS_NAME = "payroll_setup_prefs"
        const val KEY_BASIC_SALARY = "basic_salary_monthly"
        const val KEY_POSITION_ALLOWANCE = "position_allowance_monthly"
        const val KEY_MEAL_MANAGER = "meal_allowance_monthly_manager"
        const val KEY_MEAL_DAILY = "meal_allowance_daily"
        const val KEY_ATTENDANCE_MANAGER = "attendance_premium_monthly_manager"
        const val KEY_ATTENDANCE_DAILY = "attendance_premium_daily"
        const val KEY_OVERTIME_RATE = "overtime_hourly_rate"
        const val KEY_OVERTIME_PREMIUM = "overtime_premium_per_event"
    }
}
