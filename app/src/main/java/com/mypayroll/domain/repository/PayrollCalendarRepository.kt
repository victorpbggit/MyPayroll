package com.mypayroll.domain.repository

import com.mypayroll.domain.payroll.PayrollDayStatus
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

/**
 * Status Hari Kerja/Holiday per tanggal untuk Calendar Payroll. Hanya tanggal yang
 * di-override secara manual yang disimpan; tanggal lain memakai [com.mypayroll.domain.payroll.defaultPayrollDayStatus].
 */
interface PayrollCalendarRepository {
    fun observeOverrides(): Flow<Map<LocalDate, PayrollDayStatus>>
    suspend fun getOverrides(): Map<LocalDate, PayrollDayStatus>
    suspend fun statusFor(date: LocalDate): PayrollDayStatus
    suspend fun statusesFor(month: YearMonth): Map<LocalDate, PayrollDayStatus>

    /** Menyimpan override untuk [date]. [status] null = hapus override, kembali ke default. */
    suspend fun setStatus(date: LocalDate, status: PayrollDayStatus?)
}
