package com.mypayroll.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mypayroll.core.format.toDisplay
import com.mypayroll.core.format.toRupiah
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

enum class Tone { NEUTRAL, SUCCESS, WARNING, DANGER, INFO }

private fun Tone.containerColor() = when (this) {
    Tone.NEUTRAL -> Color(0xFFE3E3E3)
    Tone.SUCCESS -> Color(0xFFD7F2DD)
    Tone.WARNING -> Color(0xFFFFF0C2)
    Tone.DANGER -> Color(0xFFFBD6D6)
    Tone.INFO -> Color(0xFFD7E6FB)
}

private fun Tone.contentColor() = when (this) {
    Tone.NEUTRAL -> Color(0xFF3A3A3A)
    Tone.SUCCESS -> Color(0xFF14602A)
    Tone.WARNING -> Color(0xFF7A5B00)
    Tone.DANGER -> Color(0xFF9A2020)
    Tone.INFO -> Color(0xFF1C4C9C)
}

@Composable
fun StatusChip(text: String, tone: Tone = Tone.NEUTRAL, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = tone.containerColor()
    ) {
        Text(
            text = text,
            color = tone.contentColor(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    tone: Tone = Tone.NEUTRAL,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = tone.contentColor().takeIf { tone != Tone.NEUTRAL } ?: MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Cari...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
    )
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
        if (onDismiss != null) {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    }
}

/** Confirmation banner for a successfully completed operation - the positive counterpart to [ErrorBanner]. */
@Composable
fun InfoBanner(message: String, onDismiss: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(message, color = Tone.SUCCESS.contentColor(), modifier = Modifier.weight(1f))
        if (onDismiss != null) {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String = "Ya",
    dismissLabel: String = "Batal",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    allowClear: Boolean = false
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value?.toDisplay() ?: "",
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
        modifier = modifier.fillMaxWidth().clickable { showPicker = true }
    )

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis
                    onValueChange(millis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() })
                    showPicker = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                Row {
                    if (allowClear) {
                        TextButton(onClick = { onValueChange(null); showPicker = false }) { Text("Kosongkan") }
                    }
                    TextButton(onClick = { showPicker = false }) { Text("Batal") }
                }
            }
        ) { DatePicker(state = state) }
    }
}

@Composable
fun <T> EnumDropdownField(
    label: String,
    selected: T,
    options: List<T>,
    onSelected: (T) -> Unit,
    display: (T) -> String = { it.toString() },
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: ${display(selected)}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(display(opt)) }, onClick = { onSelected(opt); expanded = false })
            }
        }
    }
}

@Composable
fun EmployeePickerField(
    label: String,
    selected: com.mypayroll.data.local.entity.EmployeeEntity?,
    employees: List<com.mypayroll.data.local.entity.EmployeeEntity>,
    onSelected: (com.mypayroll.data.local.entity.EmployeeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected?.name ?: label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            employees.forEach { emp ->
                DropdownMenuItem(text = { Text(emp.name) }, onClick = { onSelected(emp); expanded = false })
            }
        }
    }
}

/** Simple "HH:mm" text entry. A full TimePicker dialog was skipped to keep forms fast to fill on a phone. */
@Composable
fun TimeTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> if (input.length <= 5) onValueChange(input) },
        label = { Text("$label (HH:mm)") },
        placeholder = { Text("08:00") },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackScaffold(title: String, onBack: () -> Unit, content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding -> content(padding) }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

/**
 * Input nominal Rupiah. Kotak input HANYA berisi digit mentah (tanpa "Rp" atau pemisah ribuan) -
 * memformat ulang seluruh teks di dalam kotak input pada setiap ketikan (termasuk menyisipkan
 * "Rp"/titik ribuan) menyebabkan kursor melompat ke posisi yang salah saat mengedit di tengah
 * angka. Nilai terformat ("Rp5.000.000") ditampilkan sebagai label pratinjau DI LUAR kotak input
 * (di bawahnya), memakai formatter [toRupiah] yang sudah ada, supaya tidak ada formatting yang
 * ikut masuk ke state teks yang diedit.
 */
@Composable
fun RupiahMoneyField(
    label: String,
    amount: Long,
    onAmountChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true
) {
    var text by remember(amount) { mutableStateOf(if (amount == 0L) "" else amount.toString()) }
    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                val digits = input.filter(Char::isDigit)
                text = digits
                onAmountChange(digits.toLongOrNull() ?: 0L)
            },
            label = { Text(label) },
            placeholder = { Text("0") },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            supportingText = supportingText?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            amount.toRupiah(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }
}

@Composable
fun StatRow(items: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (label, value) ->
            Card(modifier = Modifier.weight(1f)) {
                Column(Modifier.padding(10.dp)) {
                    Text(value, style = MaterialTheme.typography.titleMedium)
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
