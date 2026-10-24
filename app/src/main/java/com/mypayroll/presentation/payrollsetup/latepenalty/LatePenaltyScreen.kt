package com.mypayroll.presentation.payrollsetup.latepenalty

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
import com.mypayroll.core.format.toRupiah
import com.mypayroll.presentation.common.BackScaffold
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState

@Composable
fun LatePenaltyScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = remember { LatePenaltyViewModel(container.latePenaltyConfigRepository) }
    val state by viewModel.uiState.collectAsState()

    BackScaffold("Penalty Keterlambatan Bertingkat", onBack = onBack) { padding ->
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
                "Ketiga tingkat bersifat kumulatif: nominal dijumlahkan sesuai ambang batas yang tercapai.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PenaltyRuleCard(
                title = "Rule 1 — Keterlambatan mencapai 5 menit",
                value = state.tier1Text,
                onValueChange = { input -> viewModel.updateTier1Text(input.filter(Char::isDigit)) }
            )
            PenaltyRuleCard(
                title = "Rule 2 — Keterlambatan mencapai 15 menit",
                value = state.tier2Text,
                onValueChange = { input -> viewModel.updateTier2Text(input.filter(Char::isDigit)) }
            )
            PenaltyRuleCard(
                title = "Rule 3 — Keterlambatan mencapai 30 menit",
                value = state.tier3Text,
                onValueChange = { input -> viewModel.updateTier3Text(input.filter(Char::isDigit)) }
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = viewModel::save, enabled = !state.isSaving) {
                    Text(if (state.isSaving) "Menyimpan..." else "Simpan")
                }
            }

            CumulativeExampleTable(state.tier1Text, state.tier2Text, state.tier3Text)

            Text(
                "Catatan: penalty dihitung dari data absensi aktual (menit terlambat), tetapi BELUM memengaruhi " +
                    "payroll resmi. Payroll resmi memerlukan kolom baru untuk menyimpan nominal penalty, yang " +
                    "belum diotorisasi (lihat laporan perubahan).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun PenaltyRuleCard(title: String, value: String, onValueChange: (String) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("Nominal penalty (Rp, minimum Rp1.000)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CumulativeExampleTable(tier1: String, tier2: String, tier3: String) {
    val t1 = tier1.toLongOrNull() ?: 0L
    val t2 = tier2.toLongOrNull() ?: 0L
    val t3 = tier3.toLongOrNull() ?: 0L
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Contoh akumulasi dengan nilai di atas", style = MaterialTheme.typography.titleSmall)
            ExampleRow("Kurang dari 5 menit", 0L)
            ExampleRow("5–14 menit", t1)
            ExampleRow("15–29 menit", t1 + t2)
            ExampleRow("30 menit atau lebih", t1 + t2 + t3)
        }
    }
}

@Composable
private fun ExampleRow(label: String, amount: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(amount.toRupiah(), style = MaterialTheme.typography.bodySmall)
    }
}
