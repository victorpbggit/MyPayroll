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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.mypayroll.data.local.entity.PayrollPeriodEntity
import com.mypayroll.data.local.entity.PayrollPeriodStatus
import com.mypayroll.domain.model.PayrollLineItem
import com.mypayroll.domain.model.PayrollPolicy
import com.mypayroll.domain.usecase.ApprovePayrollUseCase
import com.mypayroll.domain.usecase.FinalizePayrollUseCase
import com.mypayroll.domain.usecase.PreviewPayrollUseCase
import com.mypayroll.presentation.common.EmployeePickerField
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.StatRow
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.Tone

@Composable
fun PayrollPeriodDetailScreen(periodId: Long, onBack: () -> Unit, onOpenCalculator: () -> Unit = {}) {
    val container = LocalAppContainer.current
    var period by remember { mutableStateOf<PayrollPeriodEntity?>(null) }
    LaunchedEffect(periodId) { period = container.payrollPeriodRepository.getById(periodId) }

    val current = period
    if (current == null) {
        LoadingState()
        return
    }
    PayrollPeriodDetailContent(current, onBack, onOpenCalculator)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayrollPeriodDetailContent(initialPeriod: PayrollPeriodEntity, onBack: () -> Unit, onOpenCalculator: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = remember(initialPeriod.id) {
        PayrollPeriodDetailViewModel(
            initialPeriod,
            PreviewPayrollUseCase(
                container.employeeRepository, container.employeeSalaryHistoryRepository,
                container.employeeIncentiveRepository, container.overtimeAssignmentRepository, container.overtimeRecordRepository, container.attendanceRepository
            ),
            ApprovePayrollUseCase(container.payrollResultRepository, container.payrollPeriodRepository, container.payrollApprovalRepository),
            FinalizePayrollUseCase(container.payrollResultRepository, container.payrollPeriodRepository, container.payrollApprovalRepository),
            container.payrollResultRepository
        )
    }
    val state by viewModel.uiState.collectAsState()
    val employees by container.employeeRepository.observeAll().collectAsState(initial = emptyList())

    var showPolicy by remember { mutableStateOf(false) }
    var approver by remember { mutableStateOf<EmployeeEntity?>(null) }
    val period = state.period

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(period.name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(period.status.name, periodToneDetail(period.status))
                Text("${period.startDate.toDisplay()} - ${period.endDate.toDisplay()}", style = MaterialTheme.typography.bodySmall)
            }
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearMessages) }
            state.infoMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }

            OutlinedButton(onClick = onOpenCalculator) { Text("Kalkulator Detail per Karyawan (GP/TJ/UM/PH/UL/PL)") }

            if (period.status == PayrollPeriodStatus.FINALIZED) {
                Text("Periode ini sudah difinalisasi. Hasil di bawah adalah snapshot final dan tidak berubah lagi meski data karyawan berubah.", style = MaterialTheme.typography.bodySmall)
                PayrollResultList(state.savedResults.map { r ->
                    PayrollLineItem(r.employeeId, employees.firstOrNull { it.id == r.employeeId }?.name ?: "Karyawan #${r.employeeId}", r.basicSalary, r.totalAllowance, r.totalIncentive, r.totalOvertimeAmount, r.attendanceDeduction, 0, 0)
                        .let { it to r }
                })
                return@Column
            }

            Button(onClick = { showPolicy = true }, enabled = !state.isCalculating) {
                Text(if (state.isCalculating) "Menghitung..." else "Hitung / Hitung Ulang Pratinjau")
            }

            if (state.preview.isNotEmpty()) {
                val totalNet = state.preview.sumOf { it.netPay }
                StatRow(listOf("Karyawan" to state.preview.size.toString(), "Total Net Pay" to totalNet.toRupiah()))
                EmployeePickerField("Disetujui oleh", approver, employees, { approver = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.approve(approver?.id) }) { Text("Setujui & Simpan") }
                    if (period.status == PayrollPeriodStatus.APPROVED) {
                        OutlinedButton(onClick = { viewModel.finalize(approver?.id) }) { Text("Finalisasi") }
                    }
                }
                Divider()
                PreviewList(state.preview)
            } else if (state.savedResults.isNotEmpty()) {
                Text("Hasil tersimpan dari perhitungan sebelumnya:", style = MaterialTheme.typography.labelMedium)
                if (period.status == PayrollPeriodStatus.APPROVED) {
                    Row { OutlinedButton(onClick = { viewModel.finalize(approver?.id) }) { Text("Finalisasi") } }
                }
                PayrollResultList(state.savedResults.map { r ->
                    PayrollLineItem(r.employeeId, employees.firstOrNull { it.id == r.employeeId }?.name ?: "Karyawan #${r.employeeId}", r.basicSalary, r.totalAllowance, r.totalIncentive, r.totalOvertimeAmount, r.attendanceDeduction, 0, 0)
                        .let { it to r }
                })
            } else {
                EmptyState("Belum ada pratinjau. Klik \"Hitung Pratinjau\" di atas.")
            }
        }
    }

    if (showPolicy) PolicyDialog(onDismiss = { showPolicy = false }, onConfirm = { policy -> showPolicy = false; viewModel.runPreview(policy) })
}

private fun periodToneDetail(status: PayrollPeriodStatus) = when (status) {
    PayrollPeriodStatus.OPEN -> Tone.NEUTRAL
    PayrollPeriodStatus.PREVIEWED -> Tone.INFO
    PayrollPeriodStatus.APPROVED -> Tone.WARNING
    PayrollPeriodStatus.FINALIZED -> Tone.SUCCESS
    PayrollPeriodStatus.CANCELLED -> Tone.DANGER
}

@Composable
private fun PreviewList(items: List<PayrollLineItem>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items, key = { it.employeeId }) { line -> PayslipCard(line) }
    }
}

@Composable
private fun PayrollResultList(items: List<Pair<PayrollLineItem, com.mypayroll.data.local.entity.PayrollResultEntity>>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items, key = { it.second.id }) { (line, result) -> PayslipCard(line.copy(), netPayOverride = result.netPay, grossPayOverride = result.grossPay, deductionOverride = result.totalDeduction) }
    }
}

@Composable
private fun PayslipCard(line: PayrollLineItem, netPayOverride: Long? = null, grossPayOverride: Long? = null, deductionOverride: Long? = null) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(line.employeeName, style = MaterialTheme.typography.titleMedium)
                Text((netPayOverride ?: line.netPay).toRupiah(), style = MaterialTheme.typography.titleMedium)
            }
            Text("Gaji pokok: ${line.basicSalary.toRupiah()}", style = MaterialTheme.typography.bodySmall)
            Text("Tunjangan/komponen: ${line.totalAllowance.toRupiah()}", style = MaterialTheme.typography.bodySmall)
            Text("Insentif: ${line.totalIncentive.toRupiah()}", style = MaterialTheme.typography.bodySmall)
            Text("Lembur: ${line.totalOvertimeAmount.toRupiah()}", style = MaterialTheme.typography.bodySmall)
            Text("Potongan: ${(deductionOverride ?: line.totalDeduction).toRupiah()}", style = MaterialTheme.typography.bodySmall)
            Text("Gross: ${(grossPayOverride ?: line.grossPay).toRupiah()}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PolicyDialog(onDismiss: () -> Unit, onConfirm: (PayrollPolicy) -> Unit) {
    var late by remember { mutableStateOf("0") }
    var absent by remember { mutableStateOf("0") }
    var bpjs by remember { mutableStateOf("0") }
    var tax by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kebijakan Payroll") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(PayrollPolicy.DISCLAIMER, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(late, { late = it.filter(Char::isDigit) }, label = { Text("Potongan per menit terlambat (Rp)") }, singleLine = true)
                OutlinedTextField(absent, { absent = it.filter(Char::isDigit) }, label = { Text("Potongan per hari absen (Rp)") }, singleLine = true)
                OutlinedTextField(bpjs, { bpjs = it }, label = { Text("Tarif BPJS karyawan (%)") }, singleLine = true)
                OutlinedTextField(tax, { tax = it }, label = { Text("Tarif pajak/PPh21 (%)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    PayrollPolicy(
                        lateDeductionPerMinute = late.toLongOrNull() ?: 0,
                        absentDeductionPerDay = absent.toLongOrNull() ?: 0,
                        bpjsEmployeeRatePercent = bpjs.toDoubleOrNull() ?: 0.0,
                        incomeTaxRatePercent = tax.toDoubleOrNull() ?: 0.0
                    )
                )
            }) { Text("Hitung") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
