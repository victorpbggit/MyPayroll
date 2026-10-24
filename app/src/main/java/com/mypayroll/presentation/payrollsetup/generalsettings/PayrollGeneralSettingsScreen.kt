package com.mypayroll.presentation.payrollsetup.generalsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState

@Composable
fun PayrollGeneralSettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = remember { PayrollGeneralSettingsViewModel(container.payrollGeneralSettingsRepository) }
    val state by viewModel.uiState.collectAsState()

    com.mypayroll.presentation.common.BackScaffold("Pengaturan Umum Payroll", onBack = onBack) { padding ->
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

            Text(
                "Dipakai untuk menurunkan GP Per Hari (GPPH) dan GP Per Jam (GPPJ) di tab Gaji Pokok " +
                    "pada Kompensasi: GPPH = GP / Days per Month, GPPJ = (GPPH x Days per Week) / Hours per Week.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SettingCard(
                title = "Currency (Curr)",
                description = "Simbol mata uang yang ditampilkan.",
                value = state.currencyText,
                onValueChange = viewModel::updateCurrencyText,
                keyboardType = KeyboardType.Text
            )
            SettingCard(
                title = "Days per Month (DM)",
                description = "Jumlah hari kerja per bulan, pembagi GP menjadi GPPH.",
                value = state.daysPerMonthText,
                onValueChange = { input -> viewModel.updateDaysPerMonthText(input.filter(Char::isDigit)) },
                keyboardType = KeyboardType.Number
            )
            SettingCard(
                title = "Days per Week (DW)",
                description = "Jumlah hari kerja per minggu, dipakai menurunkan GPPJ.",
                value = state.daysPerWeekText,
                onValueChange = { input -> viewModel.updateDaysPerWeekText(input.filter(Char::isDigit)) },
                keyboardType = KeyboardType.Number
            )
            SettingCard(
                title = "Hours per Week (HW)",
                description = "Jumlah jam kerja per minggu, pembagi GPPJ.",
                value = state.hoursPerWeekText,
                onValueChange = { input -> viewModel.updateHoursPerWeekText(input.filter(Char::isDigit)) },
                keyboardType = KeyboardType.Number
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = viewModel::save, enabled = !state.isSaving) {
                    Text(if (state.isSaving) "Menyimpan..." else "Simpan")
                }
            }
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    description: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
