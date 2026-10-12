package com.mypayroll.presentation.document

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.core.format.toDisplay
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen() {
    val container = LocalAppContainer.current
    val viewModel = remember { DocumentViewModel(container.documentRepository) }
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dokumen") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Tambah dokumen") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Hanya metadata dokumen yang disimpan - belum ada pemilih berkas (file picker) yang terhubung.",
                style = MaterialTheme.typography.bodySmall
            )
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            when {
                state.isLoading -> LoadingState()
                state.items.isEmpty() -> EmptyState("Belum ada dokumen.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items, key = { it.id }) { doc ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(doc.fileName, style = MaterialTheme.typography.titleMedium)
                                    Text("${doc.documentType} - ${doc.createdAt.toDisplay()}", style = MaterialTheme.typography.bodySmall)
                                    Text(doc.storagePath, style = MaterialTheme.typography.bodySmall)
                                }
                                TextButton(onClick = { viewModel.remove(doc) }) { Text("Hapus") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) AddDocumentDialog(onDismiss = { showAdd = false }, onSave = { name, type, path ->
        viewModel.add(name, type, path); showAdd = false
    })
}

@Composable
private fun AddDocumentDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Metadata Dokumen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama berkas") }, singleLine = true)
                OutlinedTextField(type, { type = it }, label = { Text("Jenis dokumen") }, singleLine = true)
                OutlinedTextField(path, { path = it }, label = { Text("Lokasi penyimpanan / URI") })
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank() && type.isNotBlank() && path.isNotBlank(), onClick = { onSave(name.trim(), type.trim(), path.trim()) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
