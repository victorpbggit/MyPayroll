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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.core.format.toDisplay
import com.mypayroll.core.format.toDisplayOrDash
import com.mypayroll.core.format.toRupiah
import com.mypayroll.domain.usecase.ObserveEmployeeProfileUseCase
import com.mypayroll.presentation.attendance.AttendanceScreen
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.Tone
import com.mypayroll.presentation.compensation.EmployeeCompensationScreen
import com.mypayroll.presentation.discipline.DisciplineScreen
import com.mypayroll.presentation.document.DocumentScreen
import com.mypayroll.presentation.leave.LeaveScreen
import com.mypayroll.presentation.organization.ContractMonitoringTab
import com.mypayroll.presentation.organization.PositionHistoryScreen
import com.mypayroll.presentation.overtime.OvertimeScreen

private val tabTitles = listOf("Ringkasan", "Posisi", "Kontrak", "Kompensasi", "Absensi", "Cuti", "Lembur", "Disiplin", "Payroll")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailScreen(employeeId: Long, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val profileUseCase = remember {
        ObserveEmployeeProfileUseCase(
            container.employeeRepository, container.employeePositionHistoryRepository, container.employeeContractRepository,
            container.employeeSalaryHistoryRepository, container.departmentRepository, container.positionRepository
        )
    }
    val profile by remember(employeeId) { profileUseCase(employeeId) }.collectAsState(initial = null)
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(profile?.employee?.name ?: "Detail Karyawan") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali") } },
                actions = { IconButton(onClick = { onEdit(employeeId) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
                }
            }
            val current = profile
            if (current == null) {
                LoadingState()
            } else {
                when (tab) {
                    0 -> ProfileSummaryTab(current)
                    1 -> PositionHistoryScreen(employeeId)
                    2 -> ContractMonitoringTab(employeeId)
                    3 -> EmployeeCompensationScreen(employeeId, showTopBar = false)
                    4 -> AttendanceScreen(employeeId)
                    5 -> LeaveScreen(employeeId)
                    6 -> OvertimeScreen(employeeId, showTopBar = false)
                    7 -> DisciplineScreen(employeeId, showTopBar = false)
                    else -> EmployeePayrollHistoryTab(employeeId)
                }
            }
        }
    }
}

@Composable
private fun ProfileSummaryTab(profile: com.mypayroll.domain.model.EmployeeProfile) {
    val employee = profile.employee
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(employee.name, style = MaterialTheme.typography.titleLarge)
                    StatusChip(employee.status.name, if (employee.status.name == "ACTIVE") Tone.SUCCESS else Tone.NEUTRAL)
                }
                Text("NIK: ${employee.nik}")
                employee.phone?.let { Text("Telepon: $it") }
                employee.email?.let { Text("Email: $it") }
                employee.address?.let { Text("Alamat: $it") }
                Text("Bergabung: ${employee.joinDate.toDisplay()}")
                employee.resignDate?.let { Text("Keluar: ${it.toDisplay()}") }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Jabatan Saat Ini", style = MaterialTheme.typography.titleMedium)
                Text(profile.currentPosition?.name ?: "Belum ditentukan")
                Text(profile.currentDepartment?.name ?: "-")
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Kontrak Aktif", style = MaterialTheme.typography.titleMedium)
                if (profile.activeContract != null) {
                    Text(profile.activeContract.contractNumber)
                    Text("${profile.activeContract.startDate.toDisplay()} - ${profile.activeContract.endDate.toDisplayOrDash()}")
                } else {
                    Text("Tidak ada kontrak aktif")
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Gaji Pokok Terakhir", style = MaterialTheme.typography.titleMedium)
                Text(profile.latestSalary?.basicSalary?.toRupiah() ?: "Belum ada data gaji")
            }
        }
    }
}

@Composable
private fun EmployeePayrollHistoryTab(employeeId: Long) {
    val container = LocalAppContainer.current
    val results by container.payrollResultRepository.observeByEmployee(employeeId).collectAsState(initial = emptyList())
    val periods by container.payrollPeriodRepository.observeAll().collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (results.isEmpty()) {
            EmptyState("Belum ada riwayat payroll untuk karyawan ini.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results.sortedByDescending { it.calculatedAt }, key = { it.id }) { result ->
                    val periodName = periods.firstOrNull { it.id == result.payrollPeriodId }?.name ?: "Periode #${result.payrollPeriodId}"
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(periodName, style = MaterialTheme.typography.titleMedium)
                                StatusChip(result.status.name, Tone.INFO)
                            }
                            Text("Gross: ${result.grossPay.toRupiah()}", style = MaterialTheme.typography.bodySmall)
                            Text("Net: ${result.netPay.toRupiah()}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
