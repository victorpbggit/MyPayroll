package com.mypayroll.presentation.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.core.format.toDisplay
import com.mypayroll.data.local.entity.ApprovalStatus
import com.mypayroll.data.local.entity.AttendanceExceptionType
import com.mypayroll.data.local.entity.AttendanceStatus
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EmployeePickerField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.EnumDropdownField
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.SectionLabel
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.Tone
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(employeeId: Long? = null, showTopBar: Boolean = employeeId == null) {
    val container = LocalAppContainer.current
    var tab by remember { mutableIntStateOf(0) }

    // Both ViewModels are created here, above the tab switch, so the staged attendance list and its
    // per-employee edits survive a trip to the "Pengecualian" tab and back.
    val attendanceViewModel = remember(employeeId) {
        AttendanceViewModel(employeeId, container.attendanceRepository, container.employeeRepository)
    }
    val exceptionViewModel = remember(employeeId) {
        AttendanceExceptionViewModel(employeeId, container.attendanceExceptionRepository, container.employeeRepository)
    }

    val content: @Composable () -> Unit = {
        Column(Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Absensi") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Pengecualian") })
            }
            if (tab == 0) AttendanceTab(attendanceViewModel) else AttendanceExceptionTab(exceptionViewModel)
        }
    }
    if (showTopBar) {
        Scaffold(topBar = { TopAppBar(title = { Text("Absensi") }) }) { padding ->
            Column(Modifier.padding(padding)) { content() }
        }
    } else {
        content()
    }
}

private fun attendanceTone(status: AttendanceStatus) = when (status) {
    AttendanceStatus.PRESENT -> Tone.SUCCESS
    AttendanceStatus.LATE -> Tone.WARNING
    AttendanceStatus.ABSENT, AttendanceStatus.UNEXCUSED -> Tone.DANGER
    AttendanceStatus.PERMITTED, AttendanceStatus.SICK, AttendanceStatus.LEAVE -> Tone.INFO
    AttendanceStatus.HOLIDAY -> Tone.NEUTRAL
}

@Composable
private fun AttendanceTab(viewModel: AttendanceViewModel) {
    val state by viewModel.uiState.collectAsState()
    var editTarget by remember { mutableStateOf<Long?>(null) }
    var showAllSaved by remember { mutableStateOf(false) }

    val savedRows = remember(state.rows, state.workDate, showAllSaved) {
        if (showAllSaved) state.rows else state.rows.filter { it.attendance.workDate == state.workDate }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
        state.infoMessage?.let { message ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                TextButton(onClick = viewModel::clearInfo) { Text("Tutup") }
            }
        }

        if (state.isLoading) {
            LoadingState()
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    DateField("Tanggal absensi", state.workDate, { it?.let(viewModel::setWorkDate) })
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = viewModel::loadEmployees, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.People, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Load Karyawan")
                        }
                        if (state.drafts.isNotEmpty()) {
                            OutlinedButton(onClick = viewModel::clearDrafts) { Text("Kosongkan") }
                        }
                    }
                }
                item { SectionLabel("Daftar Karyawan (${state.drafts.size})") }
                if (state.drafts.isEmpty()) {
                    item { EmptyState("Tekan \"Load Karyawan\" untuk mengambil semua karyawan aktif.") }
                }
                items(state.drafts, key = { it.employeeId }) { draft ->
                    AttendanceDraftCard(
                        draft = draft,
                        onEdit = { editTarget = draft.employeeId },
                        onRemove = { viewModel.removeDraft(draft.employeeId) }
                    )
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel(if (showAllSaved) "Tersimpan (semua tanggal)" else "Tersimpan ${state.workDate.toDisplay()}")
                        TextButton(onClick = { showAllSaved = !showAllSaved }) {
                            Text(if (showAllSaved) "Tanggal ini" else "Semua tanggal")
                        }
                    }
                }
                if (savedRows.isEmpty()) {
                    item { EmptyState("Belum ada data absensi tersimpan.") }
                }
                items(savedRows, key = { it.attendance.id }) { row ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(row.employeeName, style = MaterialTheme.typography.titleMedium)
                                StatusChip(row.attendance.status.name, attendanceTone(row.attendance.status))
                            }
                            Text(row.attendance.workDate.toDisplay(), style = MaterialTheme.typography.bodySmall)
                            if (row.attendance.lateMinutes > 0) Text("Terlambat ${row.attendance.lateMinutes} menit", style = MaterialTheme.typography.bodySmall)
                            if (row.attendance.earlyLeaveMinutes > 0) Text("Pulang cepat ${row.attendance.earlyLeaveMinutes} menit", style = MaterialTheme.typography.bodySmall)
                            row.attendance.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }

            Button(
                onClick = viewModel::saveAll,
                enabled = state.drafts.isNotEmpty() && !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isSaving) "Menyimpan..." else "Simpan ${state.drafts.size} Absensi")
            }
        }
    }

    val editing = state.drafts.firstOrNull { it.employeeId == editTarget }
    if (editing != null) {
        RecordAttendanceDialog(
            draft = editing,
            workDate = state.workDate,
            onDismiss = { editTarget = null },
            onSave = { status, late, early, notes ->
                viewModel.editDraft(editing.employeeId, status, late, early, notes)
                editTarget = null
            }
        )
    }
}

@Composable
private fun AttendanceDraftCard(draft: AttendanceDraft, onEdit: () -> Unit, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(draft.employeeName, style = MaterialTheme.typography.titleMedium)
                    Text("NIK ${draft.nik}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(draft.status.name, attendanceTone(draft.status))
            }
            val details = buildList {
                if (draft.lateMinutes > 0) add("Terlambat ${draft.lateMinutes} menit")
                if (draft.earlyLeaveMinutes > 0) add("Pulang cepat ${draft.earlyLeaveMinutes} menit")
            }
            if (details.isNotEmpty()) Text(details.joinToString(" - "), style = MaterialTheme.typography.bodySmall)
            draft.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            if (!draft.edited) {
                Text("Belum diedit (default ${AttendanceStatus.PRESENT.name})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (draft.alreadyRecorded) {
                Text("Sudah ada data pada tanggal ini - akan diperbarui", style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onEdit) { Text("Catat Absensi") }
                IconButton(onClick = onRemove) { Icon(Icons.Filled.Delete, contentDescription = "Hapus dari daftar") }
            }
        }
    }
}

/** Per-employee editor. The date comes from the tab's date picker and is shown as a label only. */
@Composable
private fun RecordAttendanceDialog(
    draft: AttendanceDraft,
    workDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (AttendanceStatus, Int, Int, String?) -> Unit
) {
    var status by remember(draft.employeeId) { mutableStateOf(draft.status) }
    var late by remember(draft.employeeId) { mutableStateOf(draft.lateMinutes.toString()) }
    var early by remember(draft.employeeId) { mutableStateOf(draft.earlyLeaveMinutes.toString()) }
    var notes by remember(draft.employeeId) { mutableStateOf(draft.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Absensi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(draft.employeeName, style = MaterialTheme.typography.titleMedium)
                Text("Tanggal: ${workDate.toDisplay()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                EnumDropdownField("Status", status, AttendanceStatus.entries, { status = it })
                OutlinedTextField(late, { late = it.filter(Char::isDigit) }, label = { Text("Menit terlambat") }, singleLine = true)
                OutlinedTextField(early, { early = it.filter(Char::isDigit) }, label = { Text("Menit pulang cepat") }, singleLine = true)
                OutlinedTextField(notes, { notes = it }, label = { Text("Catatan") })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(status, late.toIntOrNull() ?: 0, early.toIntOrNull() ?: 0, notes) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceExceptionTab(viewModel: AttendanceExceptionViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var decideTarget by remember { mutableStateOf<com.mypayroll.data.local.entity.AttendanceExceptionEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Ajukan pengecualian") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            when {
                state.isLoading -> LoadingState()
                state.rows.isEmpty() -> EmptyState("Belum ada pengecualian absensi.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.rows, key = { it.exception.id }) { row ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(row.employeeName, style = MaterialTheme.typography.titleMedium)
                                    StatusChip(
                                        row.exception.status.name,
                                        when (row.exception.status) {
                                            ApprovalStatus.APPROVED -> Tone.SUCCESS
                                            ApprovalStatus.REJECTED, ApprovalStatus.CANCELLED -> Tone.DANGER
                                            ApprovalStatus.PENDING -> Tone.WARNING
                                        }
                                    )
                                }
                                Text("${row.exception.date.toDisplay()} - ${row.exception.type.name}", style = MaterialTheme.typography.bodySmall)
                                row.exception.reason?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                if (row.exception.status == ApprovalStatus.PENDING) {
                                    Row {
                                        TextButton(onClick = { decideTarget = row.exception }) { Text("Tinjau") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddExceptionDialog(state.employees, onDismiss = { showAdd = false }, onSave = { emp, date, type, reason ->
        viewModel.submit(emp, date, type, reason)
        showAdd = false
    })

    decideTarget?.let { exception ->
        var approver by remember { mutableStateOf<EmployeeEntity?>(null) }
        AlertDialog(
            onDismissRequest = { decideTarget = null },
            title = { Text("Tinjau Pengecualian Absensi") },
            text = {
                Column {
                    Text("${exception.type.name} - ${exception.date.toDisplay()}")
                    EmployeePickerField("Disetujui/ditolak oleh", approver, state.employees, { approver = it })
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { viewModel.decide(exception, true, approver?.id); decideTarget = null }) { Text("Setujui") }
                    TextButton(onClick = { viewModel.decide(exception, false, approver?.id); decideTarget = null }) { Text("Tolak") }
                }
            },
            dismissButton = { TextButton(onClick = { decideTarget = null }) { Text("Tutup") } }
        )
    }
}

@Composable
private fun AddExceptionDialog(
    employees: List<EmployeeEntity>,
    onDismiss: () -> Unit,
    onSave: (Long, LocalDate, AttendanceExceptionType, String?) -> Unit
) {
    var employee by remember { mutableStateOf(employees.firstOrNull()) }
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var type by remember { mutableStateOf(AttendanceExceptionType.PERMITTED) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajukan Pengecualian Absensi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmployeePickerField("Pilih karyawan", employee, employees, { employee = it })
                DateField("Tanggal", date, { date = it })
                EnumDropdownField("Jenis", type, AttendanceExceptionType.entries, { type = it })
                OutlinedTextField(reason, { reason = it }, label = { Text("Alasan") })
            }
        },
        confirmButton = {
            Button(enabled = employee != null && date != null, onClick = { onSave(employee!!.id, date!!, type, reason) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
