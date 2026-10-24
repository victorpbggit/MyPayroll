package com.mypayroll.presentation.compensation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.core.format.toDisplay
import com.mypayroll.core.format.toRupiah
import com.mypayroll.data.local.entity.EmployeeIncentiveEntity
import com.mypayroll.data.local.entity.EmployeeSalaryHistoryEntity
import com.mypayroll.data.local.entity.EmployeeStatus
import com.mypayroll.data.local.entity.EmployeeVariableSalaryHistoryEntity
import com.mypayroll.data.local.entity.IncentiveTypeEntity
import com.mypayroll.domain.model.EmployeeProfile
import com.mypayroll.domain.model.PayrollGeneralSettings
import com.mypayroll.domain.payroll.gajiPokokPerHari
import com.mypayroll.domain.payroll.gajiPokokPerJam
import com.mypayroll.domain.payroll.splitVariableSalaryDaily
import com.mypayroll.domain.usecase.ObserveEmployeeProfileUseCase
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.InfoBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.RupiahMoneyField
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.Tone
import java.time.LocalDate

private val tabTitles = listOf("Ringkasan", "Gaji Pokok(GP)", "Gaji Variable(GV)", "Lembur", "Insentif")
private val defaultCellWidth = 92.dp
private val dateCellWidth = 112.dp

/**
 * Screen Kompensasi penuh - header sendiri (tombol kembali, judul, menu tindakan) dan 5 tab
 * horizontal: Ringkasan/Gaji Pokok(GP)/Gaji Variable(GV)/Lembur/Insentif.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeCompensationScreen(employeeId: Long, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = remember(employeeId) {
        EmployeeCompensationViewModel(
            employeeId,
            ObserveEmployeeProfileUseCase(
                container.employeeRepository,
                container.employeePositionHistoryRepository,
                container.employeeContractRepository,
                container.employeeSalaryHistoryRepository,
                container.departmentRepository,
                container.positionRepository
            ),
            container.employeeRepository,
            container.employeeSalaryHistoryRepository,
            container.employeeVariableSalaryHistoryRepository,
            container.employeeIncentiveRepository,
            container.incentiveTypeRepository,
            container.payrollGeneralSettingsRepository
        )
    }
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var showAddSalary by remember { mutableStateOf(false) }
    var showAddVariableSalary by remember { mutableStateOf(false) }
    var showAddIncentive by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kompensasi") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali") }
                },
                actions = {
                    IconButton(onClick = { showActionMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu tindakan")
                    }
                    DropdownMenu(expanded = showActionMenu, onDismissRequest = { showActionMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Info komponen kompensasi") },
                            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                            onClick = { showActionMenu = false; showInfoDialog = true }
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.fillMaxSize().padding(padding))
            state.employeeNotFound -> EmptyState(
                "Data karyawan tidak ditemukan untuk ID ini.",
                Modifier.fillMaxSize().padding(padding)
            )
            else -> {
                val profile = state.profile!!
                Column(Modifier.fillMaxSize().padding(padding)) {
                    EmployeeIdentityCard(profile)

                    state.errorMessage?.let { msg ->
                        ErrorBanner(msg, onDismiss = viewModel::clearMessages, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                    state.infoMessage?.let { msg ->
                        InfoBanner(msg, onDismiss = viewModel::clearMessages, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }

                    ScrollableTabRow(selectedTabIndex = tab) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
                        }
                    }

                    Box(Modifier.fillMaxSize()) {
                        when (tab) {
                            0 -> RingkasanTab(state)
                            1 -> GajiPokokTab(state, onAdd = { showAddSalary = true })
                            2 -> VariableTab(state, onAdd = { showAddVariableSalary = true })
                            3 -> LemburTab(state, onSave = viewModel::updateOvertimeRates)
                            else -> InsentifTab(state, onAdd = { showAddIncentive = true })
                        }
                    }
                }
            }
        }
    }

    if (showAddSalary) {
        AddSalaryDialog(
            tjEnabled = state.isPayrollTypeMonthly,
            onDismiss = { showAddSalary = false },
            onSave = { date, gp, tj, reason ->
                viewModel.addSalary(date, gp, tj, reason)
                showAddSalary = false
            }
        )
    }
    if (showAddVariableSalary) {
        AddVariableSalaryDialog(
            daysPerMonth = state.generalSettings.daysPerMonth,
            onDismiss = { showAddVariableSalary = false },
            onSave = { date, um, ut, uh, reason ->
                viewModel.addVariableSalary(date, um, ut, uh, reason)
                showAddVariableSalary = false
            }
        )
    }
    if (showAddIncentive) {
        AddIncentiveDialog(
            incentiveTypes = state.incentiveTypes,
            onDismiss = { showAddIncentive = false },
            onSave = { date, amount, typeId, reason ->
                viewModel.addIncentive(date, amount, typeId, reason)
                showAddIncentive = false
            }
        )
    }
    if (showInfoDialog) {
        InfoKomponenDialog(onDismiss = { showInfoDialog = false })
    }
}

@Composable
private fun EmployeeIdentityCard(profile: EmployeeProfile) {
    val employee = profile.employee
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(employee.name, style = MaterialTheme.typography.titleLarge)
                StatusChip(
                    employee.status.name,
                    if (employee.status == EmployeeStatus.ACTIVE) Tone.SUCCESS else Tone.NEUTRAL
                )
            }
            Text("NIK: ${employee.nik}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "${profile.currentPosition?.name ?: "Posisi belum ditentukan"} - ${profile.currentDepartment?.name ?: "Departemen belum ditentukan"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Payroll Type: ${employee.payrollType?.let { if (it.name == "MONTHLY") "Bulanan" else "Harian" } ?: "Belum dipilih"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Table & shared helpers
// ---------------------------------------------------------------------------------------------

@Composable
private fun TableHeaderCell(text: String, width: Dp = defaultCellWidth) {
    Text(text, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(width).padding(horizontal = 6.dp))
}

@Composable
private fun TableCell(text: String, width: Dp = defaultCellWidth) {
    Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(width).padding(horizontal = 6.dp))
}

@Composable
private fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SectionHeaderRow(title: String, onAddClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onAddClick) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Tambah")
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tab 1: Ringkasan
// ---------------------------------------------------------------------------------------------

@Composable
private fun RingkasanTab(state: EmployeeCompensationUiState) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Total Kompensasi", style = MaterialTheme.typography.titleMedium)
                    val total = state.totalKompensasi
                    if (total == null) {
                        Text("Belum dapat dihitung - Gaji Pokok belum tersedia.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text(total.toRupiah(), style = MaterialTheme.typography.headlineSmall)
                    }
                    Text(
                        "Gabungan Gaji Pokok+TJ, estimasi bulanan Gaji Variable, dan total seluruh riwayat Insentif. " +
                            "Tidak termasuk tab Lembur - itu hanya info pengaturan tarif lembur, bukan komponen yang didapat tiap bulan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            val salary = state.latestSalary
            RingkasanComponentCard(
                title = "Gaji Pokok(GP) + T.Jabatan(TJ)",
                value = salary?.let { (it.basicSalary + it.positionAllowance).toRupiah() },
                subtitle = salary?.let {
                    "GP ${it.basicSalary.toRupiah()} + TJ ${it.positionAllowance.toRupiah()} - berlaku sejak ${it.effectiveDate.toDisplay()}"
                } ?: "Belum ada data gaji pokok."
            )
        }
        item {
            val latestGv = state.latestVariableSalary
            RingkasanComponentCard(
                title = "Gaji Variable (GV)",
                value = latestGv?.let { (it.uangMakan + it.uangTransport + it.uangHadir).toRupiah() },
                subtitle = latestGv?.let {
                    "UM ${it.uangMakan.toRupiah()} / UT ${it.uangTransport.toRupiah()} / UH ${it.uangHadir.toRupiah()} per hari - berlaku sejak ${it.effectiveDate.toDisplay()}"
                } ?: "Belum ada data Gaji Variable."
            )
        }
        item {
            RingkasanComponentCard(
                title = "Kompensasi Lembur (UL/PL)",
                value = "UL ${state.overtimeRateOrDefault.toRupiah()} / PL ${state.overtimePremiumOrDefault.toRupiah()}",
                subtitle = "Info pengaturan tarif lembur (default dari GPPH) - tidak dihitung dalam Total Kompensasi."
            )
        }
        item {
            val totalIncentive = state.incentives.sumOf { it.amount }
            RingkasanComponentCard(
                title = "Insentif",
                value = if (state.incentives.isEmpty()) null else totalIncentive.toRupiah(),
                subtitle = if (state.incentives.isEmpty()) {
                    "Belum ada insentif tercatat."
                } else {
                    "Total seluruh riwayat (${state.incentives.size} entri)."
                }
            )
        }
    }
}

@Composable
private fun RingkasanComponentCard(title: String, value: String?, subtitle: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (value == null) StatusChip("Belum tersedia", Tone.WARNING)
            }
            if (value != null) Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tab 2: Gaji Pokok(GP) - GP selalu bulanan; TJ hanya jika Payroll Type Bulanan; GPPH/GPPJ turunan
// ---------------------------------------------------------------------------------------------

@Composable
private fun GajiPokokTab(state: EmployeeCompensationUiState, onAdd: () -> Unit) {
    val settings = state.generalSettings
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeaderRow("Gaji Pokok(GP)", onAdd) }
        item { GajiPokokCurrentCard(state.latestSalary, settings) }
        item { Text("Riwayat", style = MaterialTheme.typography.titleSmall) }
        if (state.salaryHistory.isEmpty()) {
            item { EmptyState("Belum ada riwayat gaji pokok.") }
        } else {
            item { GajiPokokHistoryTable(state.salaryHistory, settings) }
        }
    }
}

@Composable
private fun GajiPokokCurrentCard(latestSalary: EmployeeSalaryHistoryEntity?, settings: PayrollGeneralSettings) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Nilai berlaku saat ini", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (latestSalary == null) {
                Text("Belum ada data gaji pokok.", style = MaterialTheme.typography.bodyMedium)
            } else {
                val gpph = gajiPokokPerHari(latestSalary.basicSalary, settings.daysPerMonth)
                val gppj = gajiPokokPerJam(gpph, settings.daysPerWeek, settings.hoursPerWeek)
                Text(latestSalary.basicSalary.toRupiah(), style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Per bulan - berlaku sejak ${latestSalary.effectiveDate.toDisplay()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    LabeledValue("TJ", latestSalary.positionAllowance.toRupiah())
                    LabeledValue("GPPH", gpph.toRupiah())
                    LabeledValue("GPPJ", gppj.toRupiah())
                }
            }
        }
    }
}

@Composable
private fun GajiPokokHistoryTable(items: List<EmployeeSalaryHistoryEntity>, settings: PayrollGeneralSettings) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp).horizontalScroll(rememberScrollState())) {
            Row {
                TableHeaderCell("GP")
                TableHeaderCell("TJ")
                TableHeaderCell("GPPH")
                TableHeaderCell("GPPJ")
                TableHeaderCell("Tgl Berlaku", dateCellWidth)
            }
            Divider(Modifier.padding(vertical = 4.dp))
            items.forEach { item ->
                val gpph = gajiPokokPerHari(item.basicSalary, settings.daysPerMonth)
                val gppj = gajiPokokPerJam(gpph, settings.daysPerWeek, settings.hoursPerWeek)
                Row(Modifier.padding(vertical = 6.dp)) {
                    TableCell(item.basicSalary.toRupiah())
                    TableCell(item.positionAllowance.toRupiah())
                    TableCell(gpph.toRupiah())
                    TableCell(gppj.toRupiah())
                    TableCell(item.effectiveDate.toDisplay(), dateCellWidth)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tab 3: Gaji Variable(GV) - GV bulanan dibagi proporsional ke UM/UT/UH lalu dibagi DM (per hari)
// ---------------------------------------------------------------------------------------------

@Composable
private fun VariableTab(state: EmployeeCompensationUiState, onAdd: () -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeaderRow("Gaji Variable(GV)", onAdd) }
        item { GajiVariableCurrentCard(state.latestVariableSalary) }
        item { Text("Riwayat", style = MaterialTheme.typography.titleSmall) }
        if (state.variableSalaryHistory.isEmpty()) {
            item { EmptyState("Belum ada riwayat Gaji Variable.") }
        } else {
            item { GajiVariableHistoryTable(state.variableSalaryHistory) }
        }
    }
}

@Composable
private fun GajiVariableCurrentCard(latest: EmployeeVariableSalaryHistoryEntity?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Nilai berlaku saat ini (per hari)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (latest == null) {
                Text("Belum ada data Gaji Variable.", style = MaterialTheme.typography.bodyMedium)
            } else {
                val totalPerHari = latest.uangMakan + latest.uangTransport + latest.uangHadir
                Text(totalPerHari.toRupiah(), style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Total UM+UT+UH per hari - berlaku sejak ${latest.effectiveDate.toDisplay()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    LabeledValue("UM", latest.uangMakan.toRupiah())
                    LabeledValue("UT", latest.uangTransport.toRupiah())
                    LabeledValue("UH", latest.uangHadir.toRupiah())
                }
            }
        }
    }
}

@Composable
private fun GajiVariableHistoryTable(items: List<EmployeeVariableSalaryHistoryEntity>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp).horizontalScroll(rememberScrollState())) {
            Row {
                TableHeaderCell("UM")
                TableHeaderCell("UT")
                TableHeaderCell("UH")
                TableHeaderCell("Tgl Berlaku", dateCellWidth)
            }
            Divider(Modifier.padding(vertical = 4.dp))
            items.forEach { item ->
                Row(Modifier.padding(vertical = 6.dp)) {
                    TableCell(item.uangMakan.toRupiah())
                    TableCell(item.uangTransport.toRupiah())
                    TableCell(item.uangHadir.toRupiah())
                    TableCell(item.effectiveDate.toDisplay(), dateCellWidth)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tab 4: Lembur - UL/PL, default dari GPPH, editable, tersimpan di EmployeeEntity (bukan riwayat)
// ---------------------------------------------------------------------------------------------

@Composable
private fun LemburTab(state: EmployeeCompensationUiState, onSave: (Long, Long) -> Unit) {
    var ul by remember(state.overtimeRateOrDefault) { mutableStateOf(state.overtimeRateOrDefault) }
    var pl by remember(state.overtimePremiumOrDefault) { mutableStateOf(state.overtimePremiumOrDefault) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Halaman ini mengatur komponen tarif kompensasi lembur, bukan mencatat transaksi lembur aktual. " +
                    "Pencatatan durasi/kejadian lembur tetap berada di halaman Lembur pada menu Karyawan. " +
                    "Nilai default diambil dari GPPH (${state.gpph.toRupiah()}), bisa diedit dan disimpan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RupiahMoneyField(label = "Upah Lembur (UL) per jam", amount = ul, onAmountChange = { ul = it })
                    RupiahMoneyField(label = "Premi Lembur (PL)", amount = pl, onAmountChange = { pl = it })
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { onSave(ul, pl) }) { Text("Simpan") }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tab 5: Insentif
// ---------------------------------------------------------------------------------------------

@Composable
private fun InsentifTab(state: EmployeeCompensationUiState, onAdd: () -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeaderRow("Insentif", onAdd) }
        if (state.incentives.isEmpty()) {
            item { EmptyState("Belum ada insentif.") }
        } else {
            items(state.incentives, key = { "incentive-${it.id}" }) { entry ->
                val typeName = state.incentiveTypes.firstOrNull { it.id == entry.payComponentId }?.name
                IncentiveCard(entry, typeName)
            }
        }
    }
}

@Composable
private fun IncentiveCard(entry: EmployeeIncentiveEntity, typeName: String?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.date.toDisplay(), style = MaterialTheme.typography.titleMedium)
                Text(entry.amount.toRupiah(), style = MaterialTheme.typography.titleMedium)
            }
            Text(typeName ?: "Tanpa nama insentif", style = MaterialTheme.typography.bodySmall)
            entry.reason?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------------------------

@Composable
private fun AddSalaryDialog(
    tjEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (LocalDate, Long, Long, String?) -> Unit
) {
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var gp by remember { mutableStateOf(0L) }
    var tj by remember { mutableStateOf(0L) }
    var reason by remember { mutableStateOf("") }
    val isValid = date != null && gp > 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Riwayat Gaji Pokok") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField("Berlaku sejak", date, { date = it })
                RupiahMoneyField(
                    label = "Gaji pokok (GP, per bulan)",
                    amount = gp,
                    onAmountChange = { gp = it },
                    supportingText = if (gp <= 0L) "Masukkan nominal lebih dari Rp0" else null
                )
                RupiahMoneyField(
                    label = "Tunjangan Jabatan (TJ)",
                    amount = tj,
                    onAmountChange = { tj = it },
                    enabled = tjEnabled,
                    supportingText = if (!tjEnabled) "Hanya bisa diisi jika Payroll Type karyawan = Bulanan" else null
                )
                OutlinedTextField(reason, { reason = it }, label = { Text("Alasan (opsional)") })
            }
        },
        confirmButton = {
            Button(enabled = isValid, onClick = { onSave(date!!, gp, if (tjEnabled) tj else 0L, reason) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun AddVariableSalaryDialog(
    daysPerMonth: Int,
    onDismiss: () -> Unit,
    onSave: (LocalDate, Long, Long, Long, String?) -> Unit
) {
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var gv by remember { mutableStateOf(0L) }
    var um by remember { mutableStateOf(0L) }
    var ut by remember { mutableStateOf(0L) }
    var uh by remember { mutableStateOf(0L) }
    var breakdownEditedManually by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }

    val isValid = date != null && (um + ut + uh) > 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Riwayat Gaji Variable") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField("Berlaku sejak", date, { date = it })
                RupiahMoneyField(
                    label = "Gaji Variable (GV, per bulan)",
                    amount = gv,
                    onAmountChange = { newGv ->
                        gv = newGv
                        if (!breakdownEditedManually) {
                            val (defUm, defUt, defUh) = splitVariableSalaryDaily(newGv, daysPerMonth)
                            um = defUm; ut = defUt; uh = defUh
                        }
                    }
                )
                Text(
                    "Dibagi proporsional ke UM/UT/UH, lalu dibagi Days per Month ($daysPerMonth) untuk nominal per hari " +
                        "- tanpa pecahan desimal. Masing-masing dapat diedit manual.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RupiahMoneyField(
                    label = "Uang Makan (UM) per hari",
                    amount = um,
                    onAmountChange = { um = it; breakdownEditedManually = true }
                )
                RupiahMoneyField(
                    label = "Uang Transport (UT) per hari",
                    amount = ut,
                    onAmountChange = { ut = it; breakdownEditedManually = true }
                )
                RupiahMoneyField(
                    label = "Uang Hadir (UH) per hari",
                    amount = uh,
                    onAmountChange = { uh = it; breakdownEditedManually = true }
                )
                OutlinedTextField(reason, { reason = it }, label = { Text("Alasan (opsional)") })
            }
        },
        confirmButton = {
            Button(enabled = isValid, onClick = { onSave(date!!, um, ut, uh, reason) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun AddIncentiveDialog(
    incentiveTypes: List<IncentiveTypeEntity>,
    onDismiss: () -> Unit,
    onSave: (LocalDate, Long, Long, String?) -> Unit
) {
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var amount by remember { mutableStateOf(0L) }
    var selectedType by remember { mutableStateOf<IncentiveTypeEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    val isValid = date != null && amount > 0L && selectedType != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Insentif") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField("Tanggal", date, { date = it })
                Column {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Nama Insentif: ${selectedType?.name ?: "Pilih"}")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (incentiveTypes.isEmpty()) {
                            DropdownMenuItem(text = { Text("Belum ada nama insentif") }, onClick = { expanded = false }, enabled = false)
                        }
                        incentiveTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type.name) }, onClick = { selectedType = type; expanded = false })
                        }
                    }
                }
                if (incentiveTypes.isEmpty()) {
                    Text(
                        "Belum ada nama insentif - tambahkan lewat Payroll Setup > Pengaturan Nama Insentif.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                RupiahMoneyField(
                    label = "Jumlah insentif",
                    amount = amount,
                    onAmountChange = { amount = it },
                    supportingText = if (amount <= 0L) "Masukkan nominal lebih dari Rp0" else null
                )
                OutlinedTextField(reason, { reason = it }, label = { Text("Alasan (opsional)") })
            }
        },
        confirmButton = {
            Button(enabled = isValid, onClick = { onSave(date!!, amount, selectedType!!.id, reason) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun InfoKomponenDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Info Komponen Kompensasi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("GP (Gaji Pokok) + TJ (Tunjangan Jabatan): tersimpan bersama, selalu bulanan. TJ hanya aktif jika Payroll Type = Bulanan. GPPH/GPPJ dihitung otomatis.", style = MaterialTheme.typography.bodyMedium)
                Text("GV (Gaji Variable) = UM + UT + UH per hari: tersimpan, bisa ditambah di tab Gaji Variable(GV).", style = MaterialTheme.typography.bodyMedium)
                Text("UL/PL (Kompensasi Lembur): default dari GPPH, editable dan tersimpan per karyawan (bukan riwayat).", style = MaterialTheme.typography.bodyMedium)
                Text("Insentif: tersimpan dengan Nama Insentif dari katalog Payroll Setup.", style = MaterialTheme.typography.bodyMedium)
                Text("Total Kompensasi = (GP+TJ) + estimasi bulanan GV + total Insentif. Tidak termasuk UL/PL.", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}
