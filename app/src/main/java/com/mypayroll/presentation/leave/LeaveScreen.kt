package com.mypayroll.presentation.leave

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.core.format.toDisplay
import com.mypayroll.data.local.entity.ApprovalStatus
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.data.local.entity.LeaveRequestEntity
import com.mypayroll.data.local.entity.LeaveTypeEntity
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EmployeePickerField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.EnumDropdownField
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.Tone
import com.mypayroll.presentation.reference.MasterDataScreen
import com.mypayroll.presentation.reference.MasterDataViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(employeeId: Long? = null) {
    var tab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Pengajuan Cuti") })
            if (employeeId == null) Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Jenis Cuti") })
        }
        if (tab == 0) LeaveRequestTab(employeeId) else LeaveTypeTab()
    }
}

@Composable
private fun LeaveTypeTab() {
    val container = LocalAppContainer.current
    val repo = container.leaveTypeRepository
    val viewModel = remember {
        MasterDataViewModel<LeaveTypeEntity>(
            observeAll = repo::observeAll,
            idOf = { it.id }, codeOf = { it.code }, nameOf = { it.name }, activeOf = { it.active },
            create = { code, name -> LeaveTypeEntity(code = code, name = name) },
            withActive = { e, active -> e.copy(active = active) },
            insert = repo::insert, update = repo::update, delete = repo::delete
        )
    }
    MasterDataScreen(title = "Jenis Cuti", addDialogTitle = "Tambah Jenis Cuti", viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaveRequestTab(employeeId: Long? = null) {
    val container = LocalAppContainer.current
    val viewModel = remember(employeeId) {
        LeaveRequestViewModel(employeeId, container.leaveRequestRepository, container.employeeRepository, container.leaveTypeRepository)
    }
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var decideTarget by remember { mutableStateOf<LeaveRequestEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Ajukan cuti") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            when {
                state.isLoading -> LoadingState()
                state.rows.isEmpty() -> EmptyState("Belum ada pengajuan cuti.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.rows, key = { it.request.id }) { row ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(row.employeeName, style = MaterialTheme.typography.titleMedium)
                                    StatusChip(
                                        row.request.status.name,
                                        when (row.request.status) {
                                            ApprovalStatus.APPROVED -> Tone.SUCCESS
                                            ApprovalStatus.REJECTED, ApprovalStatus.CANCELLED -> Tone.DANGER
                                            ApprovalStatus.PENDING -> Tone.WARNING
                                        }
                                    )
                                }
                                Text("${row.leaveTypeName}: ${row.request.startDate.toDisplay()} - ${row.request.endDate.toDisplay()}", style = MaterialTheme.typography.bodySmall)
                                row.request.reason?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                if (row.request.status == ApprovalStatus.PENDING) {
                                    TextButton(onClick = { decideTarget = row.request }) { Text("Tinjau") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddLeaveRequestDialog(state.employees, state.leaveTypes, onDismiss = { showAdd = false }, onSave = { emp, type, start, end, reason ->
        viewModel.submit(emp, type, start, end, reason)
        showAdd = false
    })

    decideTarget?.let { request ->
        var approver by remember { mutableStateOf<EmployeeEntity?>(null) }
        AlertDialog(
            onDismissRequest = { decideTarget = null },
            title = { Text("Tinjau Pengajuan Cuti") },
            text = {
                Column {
                    Text("${request.startDate.toDisplay()} - ${request.endDate.toDisplay()}")
                    EmployeePickerField("Disetujui/ditolak oleh", approver, state.employees, { approver = it })
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { viewModel.decide(request, true, approver?.id); decideTarget = null }) { Text("Setujui") }
                    TextButton(onClick = { viewModel.decide(request, false, approver?.id); decideTarget = null }) { Text("Tolak") }
                }
            },
            dismissButton = { TextButton(onClick = { decideTarget = null }) { Text("Tutup") } }
        )
    }
}

@Composable
private fun AddLeaveRequestDialog(
    employees: List<EmployeeEntity>,
    leaveTypes: List<LeaveTypeEntity>,
    onDismiss: () -> Unit,
    onSave: (Long, Long, LocalDate, LocalDate, String?) -> Unit
) {
    var employee by remember { mutableStateOf(employees.firstOrNull()) }
    var leaveType by remember { mutableStateOf(leaveTypes.firstOrNull()) }
    var start by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var end by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajukan Cuti") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmployeePickerField("Pilih karyawan", employee, employees, { employee = it })
                if (leaveTypes.isNotEmpty()) {
                    EnumDropdownField("Jenis cuti", leaveType!!, leaveTypes, { leaveType = it }, display = { it.name })
                } else {
                    Text("Belum ada jenis cuti. Tambahkan di tab Jenis Cuti.", style = MaterialTheme.typography.bodySmall)
                }
                DateField("Mulai", start, { start = it })
                DateField("Selesai", end, { end = it })
                OutlinedTextField(reason, { reason = it }, label = { Text("Alasan") })
            }
        },
        confirmButton = {
            Button(
                enabled = employee != null && leaveType != null && start != null && end != null && !end!!.isBefore(start),
                onClick = { onSave(employee!!.id, leaveType!!.id, start!!, end!!, reason) }
            ) { Text("Ajukan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
