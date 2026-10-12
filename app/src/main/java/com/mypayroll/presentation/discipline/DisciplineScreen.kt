package com.mypayroll.presentation.discipline

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
import com.mypayroll.data.local.entity.DisciplinaryActionType
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EmployeePickerField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.EnumDropdownField
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.Tone
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplineScreen(employeeId: Long? = null, showTopBar: Boolean = true) {
    val container = LocalAppContainer.current
    val viewModel = remember(employeeId) {
        DisciplineViewModel(employeeId, container.employeeDisciplinaryActionRepository, container.employeeRepository)
    }
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { if (showTopBar) TopAppBar(title = { Text("Disiplin") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Tambah catatan") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            when {
                state.isLoading -> LoadingState()
                state.rows.isEmpty() -> EmptyState("Belum ada catatan disiplin.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.rows, key = { it.action.id }) { row ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(row.employeeName, style = MaterialTheme.typography.titleMedium)
                                    StatusChip(row.action.actionType.name, Tone.DANGER)
                                }
                                Text("Insiden: ${row.action.incidentDate.toDisplay()} - Tindakan: ${row.action.actionDate.toDisplay()}", style = MaterialTheme.typography.bodySmall)
                                Text(row.action.description, style = MaterialTheme.typography.bodySmall)
                                row.action.validUntil?.let { Text("Berlaku hingga ${it.toDisplay()}", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddDisciplineDialog(
        employeeId = employeeId,
        employees = state.employees,
        onDismiss = { showAdd = false },
        onSave = { emp, incident, action, type, desc, issuedBy ->
            viewModel.add(emp, incident, action, type, desc, issuedBy); showAdd = false
        }
    )
}

@Composable
private fun AddDisciplineDialog(
    employeeId: Long?,
    employees: List<EmployeeEntity>,
    onDismiss: () -> Unit,
    onSave: (Long, LocalDate, LocalDate, DisciplinaryActionType, String, Long?) -> Unit
) {
    var employee by remember { mutableStateOf(employees.firstOrNull { it.id == employeeId } ?: employees.firstOrNull()) }
    var issuer by remember { mutableStateOf<EmployeeEntity?>(null) }
    var incidentDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var actionDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var type by remember { mutableStateOf(DisciplinaryActionType.VERBAL_WARNING) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Catatan Disiplin") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (employeeId == null) {
                    EmployeePickerField("Pilih karyawan", employee, employees, { employee = it })
                }
                DateField("Tanggal insiden", incidentDate, { incidentDate = it })
                DateField("Tanggal tindakan", actionDate, { actionDate = it })
                EnumDropdownField("Jenis tindakan", type, DisciplinaryActionType.entries, { type = it })
                OutlinedTextField(description, { description = it }, label = { Text("Deskripsi") })
                EmployeePickerField("Dikeluarkan oleh (opsional)", issuer, employees, { issuer = it })
            }
        },
        confirmButton = {
            Button(
                enabled = employee != null && incidentDate != null && actionDate != null && description.isNotBlank(),
                onClick = { onSave(employee!!.id, incidentDate!!, actionDate!!, type, description.trim(), issuer?.id) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
