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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.core.format.toDisplay
import com.mypayroll.core.format.toRupiah
import com.mypayroll.data.local.entity.EmployeeEntity
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import com.mypayroll.domain.payroll.CalculatePayrollUseCase
import com.mypayroll.domain.payroll.EmployeePayrollType
import com.mypayroll.domain.payroll.PayrollCalculationResult
import com.mypayroll.presentation.common.BackScaffold
import com.mypayroll.presentation.common.EmployeePickerField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.EnumDropdownField
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.SectionLabel

/** Loads the period once, then hands off to the real content so the ViewModel always starts with a valid period. */
@Composable
fun PayrollCalculatorScreen(periodId: Long, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    var period by remember { mutableStateOf<PayrollPeriodEntity?>(null) }
    androidx.compose.runtime.LaunchedEffect(periodId) { period = container.payrollPeriodRepository.getById(periodId) }

    val current = period
    if (current == null) {
        LoadingState()
        return
    }
    PayrollCalculatorContent(current, onBack)
}

@Composable
private fun PayrollCalculatorContent(period: PayrollPeriodEntity, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = remember(period.id) {
        PayrollCalculatorViewModel(
            period,
            container.employeeRepository,
            container.employeeSalaryHistoryRepository,
            container.attendanceRepository,
            CalculatePayrollUseCase()
        )
    }
    val state by viewModel.uiState.collectAsState()

    var showAddOvertime by remember { mutableStateOf(false) }
    var showAddEarning by remember { mutableStateOf(false) }
    var showAddDeduction by remember { mutableStateOf(false) }

    BackScaffold("Kalkulator Payroll Detail", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(period.name, style = MaterialTheme.typography.titleMedium)
                Text("${period.startDate.toDisplay()} - ${period.endDate.toDisplay()}", style = MaterialTheme.typography.bodySmall)
            }
            item {
                Text(
                    "Kalkulator ini menghitung rincian GP, TJ, UM, PH, UL, dan PL sesuai aturan payroll per karyawan. " +
                        "Hasilnya BELUM BISA DISIMPAN karena data payroll tersimpan (di layar sebelumnya) belum memiliki kolom " +
                        "terpisah untuk UM, PH, UL, dan PL - gunakan ini sebagai alat bantu hitung/simulasi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            }
            item {
                SectionLabel("1. Pilih Karyawan")
                EmployeePickerField("Pilih karyawan", state.selectedEmployee, state.employees, viewModel::selectEmployee)
            }

            val employee = state.selectedEmployee
            if (employee == null) {
                item { EmptyState("Pilih karyawan terlebih dahulu untuk mulai menghitung.") }
            } else if (state.isPrefilling) {
                item { LoadingState(Modifier.fillMaxWidth().padding(24.dp)) }
            } else {
                item { CalculatorForm(state.form, viewModel) }
                item {
                    SectionLabel("5. Lembur (UL & PL per kejadian)")
                    OvertimeEventsEditor(state.form.overtimeEvents, onAdd = { showAddOvertime = true }, onRemove = viewModel::removeOvertimeEvent)
                }
                item {
                    SectionLabel("6. Komponen Penambah Tambahan")
                    NamedAmountEditor(state.form.additionalEarnings, "Tambah Komponen Penambah", onAdd = { showAddEarning = true }, onRemove = viewModel::removeAdditionalEarning)
                }
                item {
                    SectionLabel("7. Komponen Pengurang (termasuk penalti/peringatan)")
                    NamedAmountEditor(state.form.deductions, "Tambah Komponen Pengurang", onAdd = { showAddDeduction = true }, onRemove = viewModel::removeDeduction)
                }
                item {
                    Button(onClick = viewModel::calculate, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.result == null) "Hitung" else "Hitung Ulang")
                    }
                }
                state.result?.let { result ->
                    item { ResultBreakdown(result) }
                }
            }
        }
    }

    if (showAddOvertime) {
        AddOvertimeEventDialog(
            onDismiss = { showAddOvertime = false },
            onSave = { hours, premium -> viewModel.addOvertimeEvent(hours, premium); showAddOvertime = false }
        )
    }
    if (showAddEarning) {
        AddNamedAmountDialog(
            title = "Tambah Komponen Penambah",
            onDismiss = { showAddEarning = false },
            onSave = { name, amount -> viewModel.addAdditionalEarning(name, amount); showAddEarning = false }
        )
    }
    if (showAddDeduction) {
        AddNamedAmountDialog(
            title = "Tambah Komponen Pengurang",
            onDismiss = { showAddDeduction = false },
            onSave = { name, amount -> viewModel.addDeduction(name, amount); showAddDeduction = false }
        )
    }
}

@Composable
private fun CalculatorForm(form: PayrollCalculatorFormState, viewModel: PayrollCalculatorViewModel) {
    val isManager = form.employeeType == EmployeePayrollType.MANAGER
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("2. Tipe Karyawan")
        EnumDropdownField(
            "Tipe", form.employeeType, EmployeePayrollType.entries,
            { type -> viewModel.updateForm { it.copy(employeeType = type) } },
            display = { if (it == EmployeePayrollType.MANAGER) "Manager" else "Non-Manager" }
        )
        Text(
            if (isManager) "Manager: GP, TJ, UM, dan PH dibayar penuh (bulanan)." else "Non-Manager: GP dan TJ diprorata, UM dan PH dihitung per hari hadir.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionLabel("3. Komponen Dasar")
        MoneyField("Gaji Pokok (GP) bulanan", form.basicSalaryMonthly) { v -> viewModel.updateForm { it.copy(basicSalaryMonthly = v) } }
        Text("Diisi otomatis dari riwayat gaji terakhir karyawan - dapat diubah.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        MoneyField("Tunjangan Jabatan (TJ) bulanan", form.positionAllowanceMonthly) { v -> viewModel.updateForm { it.copy(positionAllowanceMonthly = v) } }

        if (isManager) {
            MoneyField("Uang Makan (UM) bulanan", form.mealAllowanceMonthlyManager) { v -> viewModel.updateForm { it.copy(mealAllowanceMonthlyManager = v) } }
            MoneyField("Premi Hadir (PH) bulanan", form.attendancePremiumMonthlyManager) { v -> viewModel.updateForm { it.copy(attendancePremiumMonthlyManager = v) } }
        } else {
            MoneyField("Uang Makan (UM) per hari hadir", form.mealAllowanceDaily) { v -> viewModel.updateForm { it.copy(mealAllowanceDaily = v) } }
            MoneyField("Premi Hadir (PH) per hari hadir", form.attendancePremiumDaily) { v -> viewModel.updateForm { it.copy(attendancePremiumDaily = v) } }
        }

        SectionLabel("4. Hari Kerja & Kehadiran")
        NumberField("Hari kerja terjadwal", form.scheduledWorkDays) { v -> viewModel.updateForm { it.copy(scheduledWorkDays = v) } }
        Text("Default jumlah hari kalender pada periode ini - sesuaikan dengan hari kerja sebenarnya.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        NumberField("Hari hadir", form.attendanceDays) { v -> viewModel.updateForm { it.copy(attendanceDays = v) } }
        Text("Diisi otomatis dari data absensi (status Hadir/Terlambat) pada periode ini - dapat diubah.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        MoneyField("Tarif Upah Lembur (UL) per jam", form.overtimeHourlyRate) { v -> viewModel.updateForm { it.copy(overtimeHourlyRate = v) } }
    }
}

@Composable
private fun MoneyField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter(Char::isDigit).ifEmpty { "0" }) },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter(Char::isDigit).ifEmpty { "0" }) },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun OvertimeEventsEditor(events: List<OvertimeEventDraft>, onAdd: () -> Unit, onRemove: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        events.forEachIndexed { index, event ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Kejadian ${index + 1}: ${event.hours} jam", style = MaterialTheme.typography.bodyMedium)
                        if (event.hours > 2) Text("PL: ${event.overtimePremiumPerEvent.toRupiah()}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onRemove(index) }) { Icon(Icons.Filled.Delete, contentDescription = "Hapus kejadian lembur") }
                }
            }
        }
        OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("+ Tambah Kejadian Lembur") }
    }
}

@Composable
private fun NamedAmountEditor(items: List<NamedAmountDraft>, addLabel: String, onAdd: () -> Unit, onRemove: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEachIndexed { index, item ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(item.name, style = MaterialTheme.typography.bodyMedium)
                        Text(item.amount.toRupiah(), style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onRemove(index) }) { Icon(Icons.Filled.Delete, contentDescription = "Hapus komponen") }
                }
            }
        }
        OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text(addLabel) }
    }
}

@Composable
private fun ResultBreakdown(result: PayrollCalculationResult) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Rincian Perhitungan", style = MaterialTheme.typography.titleMedium)
            Divider()
            BreakdownRow("Gaji Pokok (GP)", result.basicSalaryPaid)
            BreakdownRow("Tunjangan Jabatan (TJ)", result.positionAllowancePaid)
            BreakdownRow("Uang Makan (UM)", result.mealAllowancePaid)
            BreakdownRow("Premi Hadir (PH)", result.attendancePremiumPaid)
            BreakdownRow("Upah Lembur (UL)", result.overtimePay)
            BreakdownRow("Premi Lembur (PL)", result.overtimePremium)
            BreakdownRow("Komponen Penambah", result.additionalEarningsTotal)
            Divider()
            BreakdownRow("Total Penghasilan", result.totalEarnings, emphasize = true)
            BreakdownRow("Komponen Pengurang", result.deductionsTotal)
            Divider()
            BreakdownRow("Take Home Pay (THP)", result.takeHomePay, emphasize = true)
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amount: Long, emphasize: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (emphasize) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium)
        Text(
            amount.toRupiah(),
            style = if (emphasize) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun AddOvertimeEventDialog(onDismiss: () -> Unit, onSave: (Int, Long) -> Unit) {
    var hours by remember { mutableStateOf("") }
    var premium by remember { mutableStateOf("0") }
    val hoursInt = hours.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kejadian Lembur") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(hours, { hours = it.filter(Char::isDigit) }, label = { Text("Durasi (jam)") }, singleLine = true)
                if (hoursInt != null && hoursInt > 2) {
                    OutlinedTextField(premium, { premium = it.filter(Char::isDigit).ifEmpty { "0" } }, label = { Text("Premi Lembur (PL) kejadian ini") }, singleLine = true)
                } else {
                    Text("PL hanya diberikan jika durasi lebih dari 2 jam.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = hoursInt != null && hoursInt >= 0,
                onClick = { onSave(hoursInt ?: 0, if ((hoursInt ?: 0) > 2) premium.toLongOrNull() ?: 0L else 0L) }
            ) { Text("Tambah") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun AddNamedAmountDialog(title: String, onDismiss: () -> Unit, onSave: (String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama komponen") }, singleLine = true)
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit).ifEmpty { "0" } }, label = { Text("Nominal (Rp)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank(), onClick = { onSave(name.trim(), amount.toLongOrNull() ?: 0L) }) { Text("Tambah") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
