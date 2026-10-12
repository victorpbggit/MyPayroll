package com.mypayroll.presentation.employee

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
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.data.local.entity.EmployeeStatus
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.EnumDropdownField
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.SearchField
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.Tone
import java.time.LocalDate

private fun statusTone(status: EmployeeStatus) = when (status) {
    EmployeeStatus.ACTIVE -> Tone.SUCCESS
    EmployeeStatus.RESIGNED -> Tone.DANGER
    EmployeeStatus.INACTIVE -> Tone.NEUTRAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreen(
    viewModel: EmployeeViewModel,
    onOpenEmployee: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Direktori Karyawan") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Tambah karyawan") }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchField(value = state.query, onValueChange = viewModel::onQueryChange, placeholder = "Cari nama atau NIK")
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            Text("Total: ${state.employees.size} karyawan", style = MaterialTheme.typography.labelMedium)
            when {
                state.isLoading -> LoadingState()
                state.employees.isEmpty() -> EmptyState("Belum ada data karyawan.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.employees, key = { it.id }) { employee ->
                        Card(onClick = { onOpenEmployee(employee.id) }, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(employee.name, style = MaterialTheme.typography.titleMedium)
                                    Text("NIK: ${employee.nik}")
                                }
                                StatusChip(employee.status.name, statusTone(employee.status))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddEmployeeDialog(
        onDismiss = { showAdd = false },
        onSave = { employee -> viewModel.save(employee); showAdd = false }
    )
}

@Composable
private fun AddEmployeeDialog(onDismiss: () -> Unit, onSave: (EmployeeEntity) -> Unit) {
    var nik by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var joinDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var status by remember { mutableStateOf(EmployeeStatus.ACTIVE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Karyawan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nik, { nik = it }, label = { Text("NIK") }, singleLine = true)
                OutlinedTextField(name, { name = it }, label = { Text("Nama") }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, label = { Text("Telepon") }, singleLine = true)
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
                OutlinedTextField(address, { address = it }, label = { Text("Alamat") })
                DateField("Tanggal masuk", joinDate, { joinDate = it })
                EnumDropdownField("Status", status, EmployeeStatus.entries, { status = it })
            }
        },
        confirmButton = {
            Button(
                enabled = nik.isNotBlank() && name.isNotBlank() && joinDate != null,
                onClick = {
                    onSave(
                        EmployeeEntity(
                            nik = nik.trim(),
                            name = name.trim(),
                            phone = phone.trim().ifBlank { null },
                            email = email.trim().ifBlank { null },
                            address = address.trim().ifBlank { null },
                            joinDate = joinDate!!,
                            status = status
                        )
                    )
                }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
