package com.mypayroll.presentation.organization

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.data.local.entity.ContractStatus
import com.mypayroll.data.local.entity.ContractTypeEntity
import com.mypayroll.data.local.entity.DepartmentEntity
import com.mypayroll.data.local.entity.PositionEntity
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.Tone
import com.mypayroll.presentation.reference.MasterDataScreen
import com.mypayroll.presentation.reference.MasterDataViewModel
import java.time.LocalDate

private val tabs = listOf("Departemen", "Posisi", "Jenis Kontrak", "Kontrak Karyawan")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, label ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(label) })
            }
        }
        when (selectedTab) {
            0 -> DepartmentTab()
            1 -> PositionTab()
            2 -> ContractTypeTab()
            else -> ContractMonitoringTab()
        }
    }
}

@Composable
private fun DepartmentTab() {
    val container = LocalAppContainer.current
    val repo = container.departmentRepository
    val viewModel = remember {
        MasterDataViewModel<DepartmentEntity>(
            observeAll = repo::observeAll,
            idOf = { it.id }, codeOf = { it.code }, nameOf = { it.name }, activeOf = { it.active },
            create = { code, name -> DepartmentEntity(code = code, name = name) },
            withActive = { e, active -> e.copy(active = active) },
            insert = repo::insert, update = repo::update, delete = repo::delete
        )
    }
    MasterDataScreen(title = "Departemen", addDialogTitle = "Tambah Departemen", viewModel = viewModel)
}

@Composable
private fun PositionTab() {
    val container = LocalAppContainer.current
    val repo = container.positionRepository
    val viewModel = remember {
        MasterDataViewModel<PositionEntity>(
            observeAll = repo::observeAll,
            idOf = { it.id }, codeOf = { it.code }, nameOf = { it.name }, activeOf = { it.active },
            create = { code, name -> PositionEntity(code = code, name = name) },
            withActive = { e, active -> e.copy(active = active) },
            insert = repo::insert, update = repo::update, delete = repo::delete
        )
    }
    MasterDataScreen(title = "Posisi", addDialogTitle = "Tambah Posisi", viewModel = viewModel)
}

@Composable
private fun ContractTypeTab() {
    val container = LocalAppContainer.current
    val repo = container.contractTypeRepository
    val viewModel = remember {
        MasterDataViewModel<ContractTypeEntity>(
            observeAll = repo::observeAll,
            idOf = { it.id }, codeOf = { it.code }, nameOf = { it.name }, activeOf = { null },
            create = { code, name -> ContractTypeEntity(code = code, name = name) },
            withActive = { e, _ -> e },
            insert = repo::insert, update = repo::update, delete = repo::delete
        )
    }
    MasterDataScreen(title = "Jenis Kontrak", addDialogTitle = "Tambah Jenis Kontrak", viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractMonitoringTab(employeeId: Long? = null) {
    val container = LocalAppContainer.current
    val viewModel = remember(employeeId) {
        EmployeeContractViewModel(
            employeeId = employeeId,
            contractRepository = container.employeeContractRepository,
            employeeRepository = container.employeeRepository,
            contractTypeRepository = container.contractTypeRepository
        )
    }
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var terminateTarget by remember { mutableStateOf<com.mypayroll.data.local.entity.EmployeeContractEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(onClick = { showAdd = true }) {
                androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = "Tambah kontrak")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            when {
                state.isLoading -> LoadingState()
                state.rows.isEmpty() -> EmptyState("Belum ada kontrak karyawan.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.rows, key = { it.contract.id }) { row ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(row.employeeName, style = MaterialTheme.typography.titleMedium)
                                Text("${row.contract.contractNumber} - ${row.contractTypeName}", style = MaterialTheme.typography.bodySmall)
                                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StatusChip(
                                        row.contract.status.name,
                                        when (row.contract.status) {
                                            ContractStatus.ACTIVE -> Tone.SUCCESS
                                            ContractStatus.EXPIRED -> Tone.WARNING
                                            ContractStatus.TERMINATED -> Tone.DANGER
                                        }
                                    )
                                    if (row.daysToExpiry != null && row.contract.status == ContractStatus.ACTIVE) {
                                        if (row.daysToExpiry in 0..30) {
                                            StatusChip("Berakhir ${row.daysToExpiry} hari lagi", Tone.WARNING)
                                        } else if (row.daysToExpiry < 0) {
                                            StatusChip("Sudah lewat tanggal akhir", Tone.DANGER)
                                        }
                                    }
                                }
                                if (row.contract.status == ContractStatus.ACTIVE) {
                                    TextButton(onClick = { terminateTarget = row.contract }) { Text("Akhiri Kontrak") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddContractDialog(
        employees = state.employees,
        contractTypes = state.contractTypes,
        fixedEmployeeId = employeeId,
        onDismiss = { showAdd = false },
        onSave = { newEmployeeId, number, typeId, start, end ->
            viewModel.addContract(newEmployeeId, number, typeId, start, end)
            showAdd = false
        }
    )

    terminateTarget?.let { contract ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { terminateTarget = null },
            title = { Text("Akhiri Kontrak ${contract.contractNumber}") },
            text = { OutlinedTextField(reason, { reason = it }, label = { Text("Alasan") }) },
            confirmButton = {
                Button(onClick = { viewModel.terminate(contract, reason.trim()); terminateTarget = null }) { Text("Akhiri") }
            },
            dismissButton = { TextButton(onClick = { terminateTarget = null }) { Text("Batal") } }
        )
    }
}

@Composable
private fun AddContractDialog(
    employees: List<com.mypayroll.data.local.entity.EmployeeEntity>,
    contractTypes: List<ContractTypeEntity>,
    fixedEmployeeId: Long? = null,
    onDismiss: () -> Unit,
    onSave: (Long, String, Long, LocalDate, LocalDate?) -> Unit
) {
    var employeeMenu by remember { mutableStateOf(false) }
    var typeMenu by remember { mutableStateOf(false) }
    var selectedEmployee by remember { mutableStateOf(employees.firstOrNull { it.id == fixedEmployeeId } ?: employees.firstOrNull()) }
    var selectedType by remember { mutableStateOf(contractTypes.firstOrNull()) }
    var number by remember { mutableStateOf("") }
    var start by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var end by remember { mutableStateOf<LocalDate?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kontrak Karyawan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (fixedEmployeeId == null) {
                    Column {
                        OutlinedButton(onClick = { employeeMenu = true }) { Text(selectedEmployee?.name ?: "Pilih karyawan") }
                        DropdownMenu(expanded = employeeMenu, onDismissRequest = { employeeMenu = false }) {
                            employees.forEach {
                                DropdownMenuItem(text = { Text(it.name) }, onClick = { selectedEmployee = it; employeeMenu = false })
                            }
                        }
                    }
                }
                Column {
                    OutlinedButton(onClick = { typeMenu = true }) { Text(selectedType?.name ?: "Pilih jenis kontrak") }
                    DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                        contractTypes.forEach {
                            DropdownMenuItem(text = { Text(it.name) }, onClick = { selectedType = it; typeMenu = false })
                        }
                    }
                }
                OutlinedTextField(number, { number = it }, label = { Text("Nomor Kontrak") }, singleLine = true)
                DateField("Tanggal Mulai", start, { start = it })
                DateField("Tanggal Akhir (opsional)", end, { end = it }, allowClear = true)
            }
        },
        confirmButton = {
            Button(
                enabled = selectedEmployee != null && selectedType != null && number.isNotBlank() && start != null,
                onClick = { onSave(selectedEmployee!!.id, number.trim(), selectedType!!.id, start!!, end) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
