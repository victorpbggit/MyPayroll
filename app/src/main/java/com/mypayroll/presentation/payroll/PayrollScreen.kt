package com.mypayroll.presentation.payroll

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.core.format.toDisplay
import com.mypayroll.data.local.entity.PayrollPeriodStatus
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.Tone
import java.time.LocalDate

private fun periodTone(status: PayrollPeriodStatus) = when (status) {
    PayrollPeriodStatus.OPEN -> Tone.NEUTRAL
    PayrollPeriodStatus.PREVIEWED -> Tone.INFO
    PayrollPeriodStatus.APPROVED -> Tone.WARNING
    PayrollPeriodStatus.FINALIZED -> Tone.SUCCESS
    PayrollPeriodStatus.CANCELLED -> Tone.DANGER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollScreen(onOpenPeriod: (Long) -> Unit, onOpenSetup: () -> Unit = {}) {
    val container = LocalAppContainer.current
    val viewModel = remember { PayrollViewModel(container.payrollPeriodRepository) }
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll") },
                actions = {
                    IconButton(onClick = onOpenSetup) {
                        Icon(Icons.Filled.Settings, contentDescription = "Payroll Setup")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Buat periode") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            when {
                state.isLoading -> LoadingState()
                state.periods.isEmpty() -> EmptyState("Belum ada periode payroll.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.periods, key = { it.id }) { period ->
                        Card(onClick = { onOpenPeriod(period.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(period.name, style = MaterialTheme.typography.titleMedium)
                                    StatusChip(period.status.name, periodTone(period.status))
                                }
                                Text("${period.startDate.toDisplay()} - ${period.endDate.toDisplay()}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddPeriodDialog(onDismiss = { showAdd = false }, onSave = { code, name, start, end ->
        viewModel.createPeriod(code, name, start, end); showAdd = false
    })
}

@Composable
private fun AddPeriodDialog(onDismiss: () -> Unit, onSave: (String, String, LocalDate, LocalDate) -> Unit) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf<LocalDate?>(LocalDate.now().withDayOfMonth(1)) }
    var end by remember { mutableStateOf<LocalDate?>(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Periode Payroll") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(code, { code = it }, label = { Text("Kode periode (contoh: 2026-01)") }, singleLine = true)
                OutlinedTextField(name, { name = it }, label = { Text("Nama periode") }, singleLine = true)
                DateField("Mulai", start, { start = it })
                DateField("Akhir", end, { end = it })
            }
        },
        confirmButton = {
            Button(
                enabled = code.isNotBlank() && name.isNotBlank() && start != null && end != null && !end!!.isBefore(start),
                onClick = { onSave(code.trim(), name.trim(), start!!, end!!) }
            ) { Text("Buat") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
