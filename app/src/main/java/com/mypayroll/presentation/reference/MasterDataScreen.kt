package com.mypayroll.presentation.reference

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
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import com.mypayroll.presentation.common.SearchField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> MasterDataScreen(
    title: String,
    addDialogTitle: String,
    viewModel: MasterDataViewModel<T>,
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val items by viewModel.items.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Tambah") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SearchField(value = state.query, onValueChange = viewModel::onQueryChange, placeholder = "Cari $title")
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            when {
                state.isLoading -> LoadingState()
                items.isEmpty() -> EmptyState("Belum ada data.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.id }) { item ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Kode: ${item.code}", style = MaterialTheme.typography.bodySmall)
                                }
                                if (item.active != null) {
                                    Switch(checked = item.active, onCheckedChange = { viewModel.toggleActive(item) })
                                }
                                TextButton(onClick = { viewModel.remove(item) }) { Text("Hapus") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AlertDialog(
        onDismissRequest = { showAdd = false },
        title = { Text(addDialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(code, { code = it }, label = { Text("Kode") }, singleLine = true)
                OutlinedTextField(name, { name = it }, label = { Text("Nama") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                enabled = code.isNotBlank() && name.isNotBlank(),
                onClick = {
                    viewModel.add(code.trim(), name.trim())
                    code = ""; name = ""; showAdd = false
                }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Batal") } }
    )
}
