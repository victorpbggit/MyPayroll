package com.mypayroll.core.format

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val idLocale = Locale("in", "ID")
private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(idLocale).apply {
    maximumFractionDigits = 0
    minimumFractionDigits = 0
}
private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))
private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale("id", "ID"))

/** Formats an integer Rupiah amount, e.g. 4500000 -> "Rp4.500.000". Amounts are always whole Rupiah, never floating point. */
fun Long.toRupiah(): String = rupiahFormat.format(this)

fun LocalDate.toDisplay(): String = format(dateFormatter)
fun LocalDate?.toDisplayOrDash(): String = this?.toDisplay() ?: "-"
fun LocalDateTime.toDisplay(): String = format(dateTimeFormatter)
fun LocalDateTime?.toDisplayOrDash(): String = this?.toDisplay() ?: "-"

fun LocalDate.monthLabel(): String =
    "${month.getDisplayName(TextStyle.FULL, Locale("id", "ID"))} $year"
