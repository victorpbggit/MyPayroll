package com.mypayroll.domain.payroll

import java.time.DayOfWeek
import java.time.LocalDate

/** Status satu tanggal pada Calendar Payroll: menentukan apakah tanggal itu Hari Kerja atau Holiday/Libur. */
enum class PayrollDayStatus { WORKDAY, HOLIDAY }

/**
 * Default status tanggal sebelum ada override manual.
 * Senin-Sabtu = Hari Kerja, Minggu = Holiday (dikonfirmasi user: Minggu default libur,
 * dan tanggal apa pun termasuk Senin-Sabtu tetap bisa diubah manual menjadi Holiday
 * untuk mengakomodasi hari libur nasional/pemerintah).
 */
fun defaultPayrollDayStatus(date: LocalDate): PayrollDayStatus =
    if (date.dayOfWeek == DayOfWeek.SUNDAY) PayrollDayStatus.HOLIDAY else PayrollDayStatus.WORKDAY
