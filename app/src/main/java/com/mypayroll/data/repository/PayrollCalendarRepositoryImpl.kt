package com.mypayroll.data.repository

import android.content.Context
import com.mypayroll.domain.payroll.PayrollDayStatus
import com.mypayroll.domain.payroll.defaultPayrollDayStatus
import com.mypayroll.domain.repository.PayrollCalendarRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Override status tanggal (Hari Kerja <-> Holiday) tidak punya Entity/tabel Room yang cocok -
 * ini bukan data per-karyawan atau per-periode payroll, melainkan satu kalender global
 * perusahaan. Disimpan di SharedPreferences sebagai daftar "yyyy-MM-dd=STATUS" yang dipisah
 * koma, pola yang sama dipakai oleh repository config global lainnya. Hanya tanggal yang berbeda dari
 * default yang disimpan (sparse), bukan satu baris untuk setiap tanggal yang pernah ada.
 */
class PayrollCalendarRepositoryImpl(context: Context) : PayrollCalendarRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _overrides = MutableStateFlow(readOverrides())

    override fun observeOverrides() = _overrides.asStateFlow()

    override suspend fun getOverrides(): Map<LocalDate, PayrollDayStatus> = _overrides.value

    override suspend fun statusFor(date: LocalDate): PayrollDayStatus =
        _overrides.value[date] ?: defaultPayrollDayStatus(date)

    override suspend fun statusesFor(month: YearMonth): Map<LocalDate, PayrollDayStatus> {
        val overrides = _overrides.value
        return (1..month.lengthOfMonth()).associate { day ->
            val date = month.atDay(day)
            date to (overrides[date] ?: defaultPayrollDayStatus(date))
        }
    }

    override suspend fun setStatus(date: LocalDate, status: PayrollDayStatus?) {
        val updated = _overrides.value.toMutableMap()
        if (status == null || status == defaultPayrollDayStatus(date)) {
            updated.remove(date)
        } else {
            updated[date] = status
        }
        writeOverrides(updated)
        _overrides.value = updated
    }

    private fun readOverrides(): Map<LocalDate, PayrollDayStatus> {
        val raw = prefs.getString(KEY_OVERRIDES, null) ?: return emptyMap()
        return raw.split(",")
            .mapNotNull { entry ->
                val parts = entry.split("=")
                if (parts.size != 2) return@mapNotNull null
                runCatching {
                    LocalDate.parse(parts[0]) to PayrollDayStatus.valueOf(parts[1])
                }.getOrNull()
            }
            .toMap()
    }

    private fun writeOverrides(overrides: Map<LocalDate, PayrollDayStatus>) {
        val raw = overrides.entries.joinToString(",") { (date, status) -> "$date=${status.name}" }
        prefs.edit().putString(KEY_OVERRIDES, raw).apply()
    }

    private companion object {
        const val PREFS_NAME = "payroll_calendar_prefs"
        const val KEY_OVERRIDES = "overrides"
    }
}
