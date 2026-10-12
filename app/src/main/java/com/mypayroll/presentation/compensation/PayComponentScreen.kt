package com.mypayroll.presentation.compensation

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypayroll.data.local.entity.CalculationType
import com.mypayroll.data.local.entity.PayComponentCategory
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.StatusChip
import com.mypayroll.presentation.common.Tone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayComponentScreen(viewModel: PayComponentViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Komponen Gaji") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Tambah") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            when {
                state.isLoading -> LoadingState()
                state.items.isEmpty() -> EmptyState("Belum ada komponen gaji.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items, key = { it.id }) { item ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                                    Switch(checked = item.active, onCheckedChange = { viewModel.toggleActive(item) })
                                }
                                Text("Kode: ${item.code}", style = MaterialTheme.typography.bodySmall)
                                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StatusChip(item.category.name, Tone.INFO)
                                    StatusChip(item.calculationType.name, Tone.NEUTRAL)
                                    if (item.taxable) StatusChip("Kena Pajak", Tone.WARNING)
                                    if (item.bpjsApplicable) StatusChip("BPJS", Tone.WARNING)
                                }
                                TextButton(onClick = { viewModel.remove(item) }) { Text("Hapus") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddPayComponentDialog(
        onDismiss = { showAdd = false },
        onSave = { code, name, category, calcType, taxable, bpjs ->
            viewModel.add(code, name, category, calcType, taxable, bpjs)
            showAdd = false
        }
    )
}

@Composable
private fun AddPayComponentDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, PayComponentCategory, CalculationType, Boolean, Boolean) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PayComponentCategory.ALLOWANCE) }
    var calcType by remember { mutableStateOf(CalculationType.FIXED) }
    var taxable by remember { mutableStateOf(true) }
    var bpjs by remember { mutableStateOf(true) }
    var categoryMenu by remember { mutableStateOf(false) }
    var calcMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Komponen Gaji") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(code, { code = it }, label = { Text("Kode") }, singleLine = true)
                OutlinedTextField(name, { name = it }, label = { Text("Nama") }, singleLine = true)

                Column {
                    OutlinedButton(onClick = { categoryMenu = true }) { Text("Kategori: ${category.name}") }
                    DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                        PayComponentCategory.entries.forEach {
                            DropdownMenuItem(text = { Text(it.name) }, onClick = { category = it; categoryMenu = false })
                        }
                    }
                }
                Column {
                    OutlinedButton(onClick = { calcMenu = true }) { Text("Perhitungan: ${calcType.name}") }
                    DropdownMenu(expanded = calcMenu, onDismissRequest = { calcMenu = false }) {
                        CalculationType.entries.forEach {
                            DropdownMenuItem(text = { Text(it.name) }, onClick = { calcType = it; calcMenu = false })
                        }
                    }
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Switch(checked = taxable, onCheckedChange = { taxable = it })
                    Text("Kena pajak")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Switch(checked = bpjs, onCheckedChange = { bpjs = it })
                    Text("Berlaku untuk BPJS")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = code.isNotBlank() && name.isNotBlank(),
                onClick = { onSave(code.trim(), name.trim(), category, calcType, taxable, bpjs) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
