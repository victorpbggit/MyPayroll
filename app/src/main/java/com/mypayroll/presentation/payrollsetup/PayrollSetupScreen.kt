package com.mypayroll.presentation.payrollsetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.presentation.common.BackScaffold
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.RupiahMoneyField
import com.mypayroll.presentation.common.SectionLabel

@Composable
fun PayrollSetupScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = remember { PayrollSetupViewModel(container.payrollSetupRepository) }
    val state by viewModel.uiState.collectAsState()

    BackScaffold("Payroll Setup", onBack = onBack) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@BackScaffold
        }

        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Nilai di bawah ini menjadi nilai awal (default) saat menghitung payroll per karyawan di Kalkulator Payroll. " +
                    "Nilai tetap bisa diubah lagi per karyawan saat menghitung.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearMessages) }
            state.savedMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }

            val config = state.config

            SetupCard("Gaji Pokok (GP)") {
                RupiahMoneyField(
                    "Gaji pokok bulanan",
                    config.basicSalaryMonthly,
                    { v -> viewModel.updateConfig { it.copy(basicSalaryMonthly = v) } },
                    supportingText = "Manager: dibayar penuh. Non-Manager: diprorata sesuai hari hadir."
                )
            }

            SetupCard("Tunjangan Jabatan (TJ)") {
                RupiahMoneyField(
                    "Tunjangan jabatan bulanan",
                    config.positionAllowanceMonthly,
                    { v -> viewModel.updateConfig { it.copy(positionAllowanceMonthly = v) } },
                    supportingText = "Manager: dibayar penuh. Non-Manager: diprorata sesuai hari hadir."
                )
            }

            SetupCard("Uang Makan (UM)") {
                SectionLabel("Manager")
                RupiahMoneyField(
                    "Nominal / bulan",
                    config.mealAllowanceMonthlyManager,
                    { v -> viewModel.updateConfig { it.copy(mealAllowanceMonthlyManager = v) } }
                )
                SectionLabel("Non Manager")
                RupiahMoneyField(
                    "Nominal / hari",
                    config.mealAllowanceDaily,
                    { v -> viewModel.updateConfig { it.copy(mealAllowanceDaily = v) } },
                    supportingText = "Dikali jumlah hari hadir."
                )
            }

            SetupCard("Premi Hadir (PH)") {
                SectionLabel("Manager")
                RupiahMoneyField(
                    "Nominal / bulan",
                    config.attendancePremiumMonthlyManager,
                    { v -> viewModel.updateConfig { it.copy(attendancePremiumMonthlyManager = v) } }
                )
                SectionLabel("Non Manager")
                RupiahMoneyField(
                    "Nominal / hari",
                    config.attendancePremiumDaily,
                    { v -> viewModel.updateConfig { it.copy(attendancePremiumDaily = v) } },
                    supportingText = "Dikali jumlah hari hadir."
                )
            }

            SetupCard("Upah Lembur (UL)") {
                RupiahMoneyField(
                    "Nominal / jam",
                    config.overtimeHourlyRate,
                    { v -> viewModel.updateConfig { it.copy(overtimeHourlyRate = v) } },
                    supportingText = "Jam pertama 100%, jam kedua 150%, jam berikutnya 200% dari tarif ini."
                )
            }

            SetupCard("Premi Lembur (PL)") {
                RupiahMoneyField(
                    "Nominal / event",
                    config.overtimePremiumPerEvent,
                    { v -> viewModel.updateConfig { it.copy(overtimePremiumPerEvent = v) } },
                    supportingText = "Dibayar satu kali per kejadian lembur jika durasi lebih dari 2 jam."
                )
            }

            Button(onClick = viewModel::save, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isSaving) "Menyimpan..." else "Simpan Payroll Setup")
            }
        }
    }
}

@Composable
private fun SetupCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
