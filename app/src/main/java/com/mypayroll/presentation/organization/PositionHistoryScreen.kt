package com.mypayroll.presentation.organization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.mypayroll.core.format.toDisplayOrDash
import com.mypayroll.data.local.entity.DepartmentEntity
import com.mypayroll.data.local.entity.PositionEntity
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.EnumDropdownField
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PositionHistoryScreen(employeeId: Long) {
    val container = LocalAppContainer.current
    val viewModel = remember(employeeId) {
        EmployeePositionHistoryViewModel(employeeId, container.employeePositionHistoryRepository, container.positionRepository, container.departmentRepository)
    }
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Tambah mutasi/promosi") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            when {
                state.isLoading -> LoadingState()
                state.rows.isEmpty() -> EmptyState("Belum ada riwayat posisi.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.rows, key = { it.entry.id }) { row ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("${row.positionName} - ${row.departmentName}", style = MaterialTheme.typography.titleMedium)
                                Text("${row.entry.startDate.toDisplay()} - ${row.entry.endDate.toDisplayOrDash()}", style = MaterialTheme.typography.bodySmall)
                                row.entry.reason?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddPositionHistoryDialog(
        positions = state.positions,
        departments = state.departments,
        onDismiss = { showAdd = false },
        onSave = { positionId, departmentId, startDate, reason ->
            viewModel.transfer(positionId, departmentId, null, startDate, reason)
            showAdd = false
        }
    )
}

@Composable
private fun AddPositionHistoryDialog(
    positions: List<PositionEntity>,
    departments: List<DepartmentEntity>,
    onDismiss: () -> Unit,
    onSave: (Long, Long, LocalDate, String?) -> Unit
) {
    var position by remember { mutableStateOf(positions.firstOrNull()) }
    var department by remember { mutableStateOf(departments.firstOrNull()) }
    var startDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mutasi / Promosi Karyawan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (positions.isEmpty() || departments.isEmpty()) {
                    Text("Tambahkan data Posisi dan Departemen dulu di menu Organisasi.")
                } else {
                    EnumDropdownField("Posisi", position!!, positions, { position = it }, display = { it.name })
                    EnumDropdownField("Departemen", department!!, departments, { department = it }, display = { it.name })
                }
                DateField("Berlaku sejak", startDate, { startDate = it })
                OutlinedTextField(reason, { reason = it }, label = { Text("Alasan (promosi/mutasi)") })
            }
        },
        confirmButton = {
            Button(
                enabled = position != null && department != null && startDate != null,
                onClick = { onSave(position!!.id, department!!.id, startDate!!, reason) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
