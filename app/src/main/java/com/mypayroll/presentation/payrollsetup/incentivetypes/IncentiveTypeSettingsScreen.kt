package com.mypayroll.presentation.payrollsetup.incentivetypes

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.mypayroll.data.local.entity.IncentiveTypeEntity
import com.mypayroll.presentation.common.EmptyState
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.InfoBanner
import com.mypayroll.presentation.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncentiveTypeSettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = remember { IncentiveTypeSettingsViewModel(container.incentiveTypeRepository) }
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Nama Insentif") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Tambah") }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearMessages) }
            state.infoMessage?.let { InfoBanner(it, onDismiss = viewModel::clearMessages) }
            Text(
                "Daftar nama insentif yang bisa dipilih saat menambah Insentif pada tab Kompensasi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.items.isEmpty()) {
                EmptyState("Belum ada nama insentif. Tambahkan lewat tombol +.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items, key = { it.id }) { item -> IncentiveTypeCard(item, onDelete = { viewModel.delete(item) }) }
                }
            }
        }
    }

    if (showAdd) {
        AddIncentiveTypeDialog(
            onDismiss = { showAdd = false },
            onSave = { name, description ->
                viewModel.add(name, description)
                showAdd = false
            }
        )
    }
}

@Composable
private fun IncentiveTypeCard(item: IncentiveTypeEntity, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                item.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Hapus") }
        }
    }
}

@Composable
private fun AddIncentiveTypeDialog(onDismiss: () -> Unit, onSave: (String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Nama Insentif") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama Insentif") }, singleLine = true)
                OutlinedTextField(description, { description = it }, label = { Text("Deskripsi (opsional)") })
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank(), onClick = { onSave(name, description) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
