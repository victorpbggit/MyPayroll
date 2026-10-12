package com.mypayroll.presentation.compensation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TopAppBar
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
import com.mypayroll.core.format.toRupiah
import com.mypayroll.data.local.entity.CalculationType
import com.mypayroll.data.local.entity.PayComponentEntity
import com.mypayroll.data.local.entity.SalaryType
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.EnumDropdownField
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeCompensationScreen(employeeId: Long, showTopBar: Boolean = true) {
    val container = LocalAppContainer.current
    val viewModel = remember(employeeId) {
        EmployeeCompensationViewModel(
            employeeId,
            container.employeeSalaryHistoryRepository,
            container.employeePayComponentHistoryRepository,
            container.employeeIncentiveRepository,
            container.payComponentRepository
        )
    }
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { if (showTopBar) TopAppBar(title = { Text("Kompensasi") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Tambah") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Gaji") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Komponen") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Insentif") })
            }
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
                when {
                    state.isLoading -> LoadingState()
                    tab == 0 -> SalaryHistoryList(state.salaryHistory)
                    tab == 1 -> PayComponentHistoryList(state.payComponentHistory)
                    else -> IncentiveList(state.incentives)
                }
            }
        }
    }

    if (showAdd) when (tab) {
        0 -> AddSalaryDialog(onDismiss = { showAdd = false }, onSave = { date, amount, type, reason ->
            viewModel.addSalary(date, amount, type, reason); showAdd = false
        })
        1 -> AddPayComponentHistoryDialog(state.payComponents, onDismiss = { showAdd = false }, onSave = { compId, date, amount, calcType, reason ->
            viewModel.addPayComponent(compId, date, amount, calcType, reason); showAdd = false
        })
        else -> AddIncentiveDialog(state.payComponents, onDismiss = { showAdd = false }, onSave = { compId, date, amount, reason ->
            viewModel.addIncentive(compId, date, amount, reason); showAdd = false
        })
    }
}

@Composable
private fun SalaryHistoryList(items: List<com.mypayroll.data.local.entity.EmployeeSalaryHistoryEntity>) {
    if (items.isEmpty()) { EmptyState("Belum ada riwayat gaji."); return }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items, key = { it.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(item.basicSalary.toRupiah(), style = MaterialTheme.typography.titleMedium)
                    Text("Berlaku sejak ${item.effectiveDate.toDisplay()} - ${item.salaryType.name}", style = MaterialTheme.typography.bodySmall)
                    item.reason?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

@Composable
private fun PayComponentHistoryList(items: List<PayComponentHistoryRow>) {
    if (items.isEmpty()) { EmptyState("Belum ada riwayat komponen gaji."); return }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items, key = { it.entry.id }) { row ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(row.componentName, style = MaterialTheme.typography.titleMedium)
                        Text(row.entry.amount.toRupiah())
                    }
                    Text("Berlaku sejak ${row.entry.effectiveDate.toDisplay()} - ${row.entry.calculationType.name}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun IncentiveList(items: List<IncentiveRow>) {
    if (items.isEmpty()) { EmptyState("Belum ada insentif."); return }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items, key = { it.entry.id }) { row ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(row.componentName, style = MaterialTheme.typography.titleMedium)
                        Text(row.entry.amount.toRupiah())
                    }
                    Text(row.entry.date.toDisplay(), style = MaterialTheme.typography.bodySmall)
                    row.entry.reason?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

@Composable
private fun AddSalaryDialog(onDismiss: () -> Unit, onSave: (LocalDate, Long, SalaryType, String?) -> Unit) {
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(SalaryType.MONTHLY) }
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Riwayat Gaji") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField("Berlaku sejak", date, { date = it })
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Gaji pokok (Rp)") }, singleLine = true)
                EnumDropdownField("Tipe", type, SalaryType.entries, { type = it })
                OutlinedTextField(reason, { reason = it }, label = { Text("Alasan") })
            }
        },
        confirmButton = {
            Button(enabled = date != null && amount.isNotBlank(), onClick = { onSave(date!!, amount.toLong(), type, reason) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun AddPayComponentHistoryDialog(
    components: List<PayComponentEntity>,
    onDismiss: () -> Unit,
    onSave: (Long, LocalDate, Long, CalculationType, String?) -> Unit
) {
    var component by remember { mutableStateOf(components.firstOrNull()) }
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var amount by remember { mutableStateOf("") }
    var calcType by remember { mutableStateOf(CalculationType.FIXED) }
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Komponen Gaji Karyawan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (components.isEmpty()) {
                    Text("Belum ada komponen gaji. Tambahkan dulu di menu Komponen Gaji.")
                } else {
                    EnumDropdownField("Komponen", component!!, components, { component = it }, display = { it.name })
                }
                DateField("Berlaku sejak", date, { date = it })
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Jumlah (Rp)") }, singleLine = true)
                EnumDropdownField("Perhitungan", calcType, CalculationType.entries, { calcType = it })
                OutlinedTextField(reason, { reason = it }, label = { Text("Alasan") })
            }
        },
        confirmButton = {
            Button(
                enabled = component != null && date != null && amount.isNotBlank(),
                onClick = { onSave(component!!.id, date!!, amount.toLong(), calcType, reason) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun AddIncentiveDialog(
    components: List<PayComponentEntity>,
    onDismiss: () -> Unit,
    onSave: (Long, LocalDate, Long, String?) -> Unit
) {
    var component by remember { mutableStateOf(components.firstOrNull()) }
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Insentif") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (components.isEmpty()) {
                    Text("Belum ada komponen gaji. Tambahkan dulu di menu Komponen Gaji.")
                } else {
                    EnumDropdownField("Komponen", component!!, components, { component = it }, display = { it.name })
                }
                DateField("Tanggal", date, { date = it })
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Jumlah (Rp)") }, singleLine = true)
                OutlinedTextField(reason, { reason = it }, label = { Text("Alasan") })
            }
        },
        confirmButton = {
            Button(
                enabled = component != null && date != null && amount.isNotBlank(),
                onClick = { onSave(component!!.id, date!!, amount.toLong(), reason) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
