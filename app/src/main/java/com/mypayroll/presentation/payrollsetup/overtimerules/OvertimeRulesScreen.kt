package com.mypayroll.presentation.payrollsetup.overtimerules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun OvertimeRulesScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = remember { OvertimeRulesViewModel(container.overtimeRuleConfigRepository) }
    val state by viewModel.uiState.collectAsState()

    BackScaffold("Presentase Perhitungan Lembur", onBack = onBack) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@BackScaffold
        }

        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearMessages) }
            state.savedMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }

            RuleCard(
                title = "Rule 1 — Lembur mencapai 1 jam",
                rule = "Jika lembur mencapai 1 jam atau lebih, gunakan persentase UL yang ditetapkan. " +
                    "Persentase minimum adalah 100% dari Gaji Pokok (GP).",
                fieldLabel = "Persentase UL (%)",
                value = state.rule1Text,
                onValueChange = { input -> viewModel.updateRule1Text(input.filter(Char::isDigit)) }
            )

            RuleCard(
                title = "Rule 2 — Lembur mencapai 2 jam",
                rule = "Jika lembur mencapai 2 jam atau lebih, gunakan persentase UL yang ditetapkan. " +
                    "Persentase minimum adalah 100% dari Gaji Pokok (GP).",
                fieldLabel = "Persentase UL (%)",
                value = state.rule2Text,
                onValueChange = { input -> viewModel.updateRule2Text(input.filter(Char::isDigit)) }
            )

            RuleCard(
                title = "Rule 3 — Lembur pada hari Holiday",
                rule = "Status tanggal pada Calendar Payroll menentukan apakah lembur terjadi pada hari kerja " +
                    "atau hari Holiday. Lembur pada tanggal berstatus Holiday memakai persentase ini.",
                fieldLabel = "Persentase Holiday (%)",
                value = state.holidayText,
                onValueChange = { input -> viewModel.updateHolidayText(input.filter(Char::isDigit)) }
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = viewModel::save, enabled = !state.isSaving) {
                    Text(if (state.isSaving) "Menyimpan..." else "Simpan")
                }
            }

            Text(
                "Catatan: nilai di atas tersimpan, tetapi BELUM diterapkan ke perhitungan payroll resmi. " +
                    "Formula resminya (persentase × GP per jam) masih menunggu konfirmasi sebelum dihubungkan " +
                    "ke Kalkulator Payroll, agar tidak ada dua formula UL yang berbeda berjalan bersamaan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun RuleCard(
    title: String,
    rule: String,
    fieldLabel: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(rule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(fieldLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
