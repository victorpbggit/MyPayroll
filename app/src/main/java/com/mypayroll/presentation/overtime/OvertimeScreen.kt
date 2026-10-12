package com.mypayroll.presentation.overtime

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.mypayroll.core.format.toRupiah
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.data.local.entity.OvertimeAssignmentEntity
import com.mypayroll.data.local.entity.OvertimeAssignmentStatus
import com.mypayroll.data.local.entity.OvertimeRecordEntity
import com.mypayroll.data.local.entity.OvertimeRecordStatus
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EmployeePickerField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.TimeTextField
import com.mypayroll.presentation.common.Tone
import java.time.LocalDate
import java.time.LocalTime

private fun assignmentTone(status: OvertimeAssignmentStatus) = when (status) {
    OvertimeAssignmentStatus.ASSIGNED -> Tone.WARNING
    OvertimeAssignmentStatus.ACCEPTED -> Tone.SUCCESS
    OvertimeAssignmentStatus.REJECTED, OvertimeAssignmentStatus.CANCELLED -> Tone.DANGER
}

private fun recordTone(status: OvertimeRecordStatus) = when (status) {
    OvertimeRecordStatus.PENDING -> Tone.WARNING
    OvertimeRecordStatus.APPROVED -> Tone.SUCCESS
    OvertimeRecordStatus.REJECTED -> Tone.DANGER
}

private fun parseTime(text: String): LocalTime? = runCatching { LocalTime.parse(text) }.getOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OvertimeScreen(employeeId: Long? = null, showTopBar: Boolean = true) {
    val container = LocalAppContainer.current
    val viewModel = remember(employeeId) {
        OvertimeViewModel(employeeId, container.overtimeAssignmentRepository, container.overtimeRecordRepository, container.employeeRepository)
    }
    val state by viewModel.uiState.collectAsState()
    var showAssign by remember { mutableStateOf(false) }
    var respondTarget by remember { mutableStateOf<OvertimeAssignmentEntity?>(null) }
    var recordTarget by remember { mutableStateOf<OvertimeAssignmentEntity?>(null) }
    var approveTarget by remember { mutableStateOf<OvertimeRecordEntity?>(null) }

    Scaffold(
        topBar = { if (showTopBar) TopAppBar(title = { Text("Lembur") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAssign = true }) { Icon(Icons.Filled.Add, contentDescription = "Tugaskan lembur") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            when {
                state.isLoading -> LoadingState()
                state.rows.isEmpty() -> EmptyState("Belum ada penugasan lembur.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.rows, key = { it.assignment.id }) { row ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(row.employeeName, style = MaterialTheme.typography.titleMedium)
                                    StatusChip(row.assignment.status.name, assignmentTone(row.assignment.status))
                                }
                                Text(
                                    "${row.assignment.workDate.toDisplay()} ${row.assignment.plannedStart}-${row.assignment.plannedEnd}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                row.assignment.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

                                if (row.assignment.status == OvertimeAssignmentStatus.ASSIGNED) {
                                    TextButton(onClick = { respondTarget = row.assignment }) { Text("Tanggapi") }
                                }
                                if (row.assignment.status == OvertimeAssignmentStatus.ACCEPTED) {
                                    TextButton(onClick = { recordTarget = row.assignment }) { Text("Catat Aktual") }
                                }

                                if (row.records.isNotEmpty()) {
                                    Divider(Modifier.padding(vertical = 6.dp))
                                    Text("Catatan aktual:", style = MaterialTheme.typography.labelMedium)
                                    row.records.forEach { record ->
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column {
                                                Text("${record.actualStart}-${record.actualEnd} (${record.approvedMinutes} menit)", style = MaterialTheme.typography.bodySmall)
                                                Text(record.calculationAmount.toRupiah(), style = MaterialTheme.typography.bodySmall)
                                            }
                                            Row {
                                                StatusChip(record.status.name, recordTone(record.status))
                                                if (record.status == OvertimeRecordStatus.PENDING) {
                                                    TextButton(onClick = { approveTarget = record }) { Text("Tinjau") }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAssign) AssignOvertimeDialog(state.employees, onDismiss = { showAssign = false }, onSave = { emp, date, start, end, desc ->
        viewModel.assign(emp, date, start, end, desc)
        showAssign = false
    })

    respondTarget?.let { assignment ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { respondTarget = null },
            title = { Text("Tanggapi Penugasan Lembur") },
            text = { OutlinedTextField(reason, { reason = it }, label = { Text("Alasan menolak (jika menolak)") }) },
            confirmButton = {
                Row {
                    TextButton(onClick = { viewModel.respond(assignment, true); respondTarget = null }) { Text("Terima") }
                    TextButton(onClick = { viewModel.respond(assignment, false, reason); respondTarget = null }) { Text("Tolak") }
                }
            },
            dismissButton = { TextButton(onClick = { respondTarget = null }) { Text("Tutup") } }
        )
    }

    recordTarget?.let { assignment ->
        RecordActualDialog(
            onDismiss = { recordTarget = null },
            onSave = { start, end, rate -> viewModel.recordActual(assignment, start, end, rate); recordTarget = null }
        )
    }

    approveTarget?.let { record ->
        var approver by remember { mutableStateOf<EmployeeEntity?>(null) }
        AlertDialog(
            onDismissRequest = { approveTarget = null },
            title = { Text("Tinjau Lembur Aktual") },
            text = { EmployeePickerField("Disetujui/ditolak oleh", approver, state.employees, { approver = it }) },
            confirmButton = {
                Row {
                    TextButton(onClick = { viewModel.decideRecord(record, true, approver?.id); approveTarget = null }) { Text("Setujui") }
                    TextButton(onClick = { viewModel.decideRecord(record, false, approver?.id); approveTarget = null }) { Text("Tolak") }
                }
            },
            dismissButton = { TextButton(onClick = { approveTarget = null }) { Text("Tutup") } }
        )
    }
}

@Composable
private fun AssignOvertimeDialog(
    employees: List<EmployeeEntity>,
    onDismiss: () -> Unit,
    onSave: (Long, LocalDate, LocalTime, LocalTime, String?) -> Unit
) {
    var employee by remember { mutableStateOf(employees.firstOrNull()) }
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var start by remember { mutableStateOf("18:00") }
    var end by remember { mutableStateOf("20:00") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tugaskan Lembur") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmployeePickerField("Pilih karyawan", employee, employees, { employee = it })
                DateField("Tanggal", date, { date = it })
                TimeTextField("Mulai rencana", start, { start = it })
                TimeTextField("Selesai rencana", end, { end = it })
                OutlinedTextField(description, { description = it }, label = { Text("Keterangan tugas") })
            }
        },
        confirmButton = {
            val startTime = parseTime(start)
            val endTime = parseTime(end)
            Button(
                enabled = employee != null && date != null && startTime != null && endTime != null,
                onClick = { onSave(employee!!.id, date!!, startTime!!, endTime!!, description) }
            ) { Text("Tugaskan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun RecordActualDialog(onDismiss: () -> Unit, onSave: (LocalTime, LocalTime, Long) -> Unit) {
    var start by remember { mutableStateOf("18:00") }
    var end by remember { mutableStateOf("20:00") }
    var rate by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Lembur Aktual") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeTextField("Mulai aktual", start, { start = it })
                TimeTextField("Selesai aktual", end, { end = it })
                OutlinedTextField(
                    rate, { rate = it.filter(Char::isDigit) },
                    label = { Text("Tarif per menit (Rp)") },
                    supportingText = { Text("Belum ada aturan tarif lembur resmi - isi manual sesuai kebijakan.") }
                )
            }
        },
        confirmButton = {
            val startTime = parseTime(start)
            val endTime = parseTime(end)
            Button(
                enabled = startTime != null && endTime != null && endTime.isAfter(startTime),
                onClick = { onSave(startTime!!, endTime!!, rate.toLongOrNull() ?: 0L) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
