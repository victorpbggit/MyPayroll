package com.mypayroll.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.core.format.toDisplay
import com.mypayroll.domain.usecase.GetDashboardSummaryUseCase
import com.mypayroll.presentation.common.SectionLabel
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.SummaryStatCard
import com.mypayroll.presentation.common.Tone

data class DashboardQuickActions(
    val onOpenEmployees: () -> Unit,
    val onOpenAttendance: () -> Unit,
    val onOpenLeave: () -> Unit,
    val onOpenOvertime: () -> Unit,
    val onOpenPayroll: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(actions: DashboardQuickActions) {
    val container = LocalAppContainer.current
    val viewModel = remember {
        DashboardViewModel(
            GetDashboardSummaryUseCase(
                container.employeeRepository, container.attendanceRepository, container.leaveRequestRepository,
                container.employeeContractRepository, container.overtimeAssignmentRepository, container.overtimeRecordRepository,
                container.payrollPeriodRepository
            )
        )
    }
    val summary by viewModel.summary.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Dashboard") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionLabel("Karyawan")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryStatCard("Total Karyawan", summary.totalEmployees.toString(), modifier = Modifier.weight(1f))
                    SummaryStatCard("Aktif", summary.activeEmployees.toString(), tone = Tone.SUCCESS, modifier = Modifier.weight(1f))
                }
            }
            item {
                SectionLabel("Absensi Hari Ini")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryStatCard("Hadir", summary.todayPresent.toString(), tone = Tone.SUCCESS, modifier = Modifier.weight(1f))
                    SummaryStatCard("Terlambat", summary.todayLate.toString(), tone = Tone.WARNING, modifier = Modifier.weight(1f))
                    SummaryStatCard("Absen", summary.todayAbsent.toString(), tone = Tone.DANGER, modifier = Modifier.weight(1f))
                }
                Text(
                    "${summary.todayNotRecorded} karyawan aktif belum tercatat absensinya hari ini (bukan berarti absen).",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            item {
                SectionLabel("Cuti & Lembur")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryStatCard("Cuti Pending", summary.pendingLeaveRequests.toString(), tone = Tone.WARNING, modifier = Modifier.weight(1f))
                    SummaryStatCard("Lembur Pending", summary.pendingOvertimeAssignments.toString(), tone = Tone.WARNING, modifier = Modifier.weight(1f))
                    SummaryStatCard("Lembur Disetujui (bulan ini)", "${summary.approvedOvertimeMinutesThisMonth} menit", modifier = Modifier.weight(1f))
                }
            }
            item {
                SectionLabel("Payroll")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(summary.payrollPeriodLabel, style = MaterialTheme.typography.titleMedium)
                        StatusChip(summary.payrollPeriodStatusLabel, Tone.INFO)
                    }
                }
            }
            if (summary.expiringContracts.isNotEmpty()) {
                item {
                    SectionLabel("Kontrak Akan Berakhir (30 hari)")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(summary.expiringContracts) { contract ->
                            Card {
                                Column(Modifier.padding(12.dp)) {
                                    Text(contract.contractNumber, style = MaterialTheme.typography.titleSmall)
                                    Text("Berakhir ${contract.endDate?.toDisplay()}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
            item {
                SectionLabel("Aksi Cepat")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = actions.onOpenEmployees) { Text("Karyawan") }
                    Button(onClick = actions.onOpenAttendance) { Text("Absensi") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(onClick = actions.onOpenLeave) { Text("Cuti") }
                    Button(onClick = actions.onOpenOvertime) { Text("Lembur") }
                    Button(onClick = actions.onOpenPayroll) { Text("Payroll") }
                }
            }
        }
    }
}
