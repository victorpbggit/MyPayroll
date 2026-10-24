package com.mypayroll.presentation.payrollsetup.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.domain.payroll.PayrollDayStatus
import com.mypayroll.presentation.common.BackScaffold
import com.mypayroll.presentation.common.ErrorBanner
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val workdayColor = Color(0xFFBDBDBD)
private val holidayColor = Color(0xFFA5D6A7)

@Composable
fun PayrollCalendarScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = remember { PayrollCalendarViewModel(container.payrollCalendarRepository) }
    val state by viewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    BackScaffold("Calendar Hari Kerja & Holiday", onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Default Senin-Sabtu adalah Hari Kerja dan Minggu adalah Holiday. Tanggal apa pun bisa diubah " +
                    "manual, misalnya untuk hari libur nasional.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Legend()

            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearMessages) }
            state.savedMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
            if (state.isSaving) {
                Text("Menyimpan...", style = MaterialTheme.typography.bodySmall)
            }

            MonthHeader(state.month, onPrevious = viewModel::previousMonth, onNext = viewModel::nextMonth)

            WeekdayHeader()

            MonthGrid(month = state.month, statuses = state.statuses, onDayClick = { selectedDate = it })
        }
    }

    val dateForDialog = selectedDate
    if (dateForDialog != null) {
        val currentStatus = state.statuses[dateForDialog] ?: PayrollDayStatus.WORKDAY
        DayStatusDialog(
            date = dateForDialog,
            currentStatus = currentStatus,
            onSelect = { status ->
                viewModel.setStatus(dateForDialog, status)
                selectedDate = null
            },
            onDismiss = { selectedDate = null }
        )
    }
}

@Composable
private fun Legend() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(workdayColor, "Hari Kerja")
        LegendItem(holidayColor, "Holiday")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.height(16.dp).aspectRatio(1f).background(color, RoundedCornerShape(4.dp)))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Bulan sebelumnya") }
        Text(
            "${month.month.getDisplayName(TextStyle.FULL, Locale("id", "ID"))} ${month.year}",
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onNext) { Icon(Icons.Filled.ChevronRight, contentDescription = "Bulan berikutnya") }
    }
}

private val weekdayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth()) {
        weekdayLabels.forEach { label ->
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MonthGrid(month: YearMonth, statuses: Map<LocalDate, PayrollDayStatus>, onDayClick: (LocalDate) -> Unit) {
    val firstDay = month.atDay(1)
    val leadingBlanks = firstDay.dayOfWeek.value - 1 // Senin = 1 -> 0 blanks
    val totalCells = leadingBlanks + month.lengthOfMonth()
    val rows = (totalCells + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - leadingBlanks + 1
                    if (dayNumber in 1..month.lengthOfMonth()) {
                        val date = month.atDay(dayNumber)
                        val status = statuses[date] ?: PayrollDayStatus.WORKDAY
                        DayCell(dayNumber, status, Modifier.weight(1f)) { onDayClick(date) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, status: PayrollDayStatus, modifier: Modifier, onClick: () -> Unit) {
    val color = if (status == PayrollDayStatus.HOLIDAY) holidayColor else workdayColor
    Box(
        modifier
            .aspectRatio(1f)
            .background(color, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(day.toString(), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DayStatusDialog(
    date: LocalDate,
    currentStatus: PayrollDayStatus,
    onSelect: (PayrollDayStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(date.toString()) },
        text = { Text("Status saat ini: ${currentStatus.label()}. Pilih status baru:") },
        confirmButton = {
            TextButton(onClick = { onSelect(PayrollDayStatus.HOLIDAY) }) { Text("Jadikan Holiday") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onSelect(PayrollDayStatus.WORKDAY) }) { Text("Jadikan Hari Kerja") }
                TextButton(onClick = onDismiss) { Text("Batal") }
            }
        }
    )
}
