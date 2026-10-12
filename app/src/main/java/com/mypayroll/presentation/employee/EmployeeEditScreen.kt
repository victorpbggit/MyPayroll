package com.mypayroll.presentation.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.data.local.entity.EmployeeStatus
import com.mypayroll.data.local.entity.Gender
import com.mypayroll.presentation.common.DateField
import com.mypayroll.presentation.common.EnumDropdownField
import com.mypayroll.presentation.common.ErrorBanner
import com.mypayroll.presentation.common.LoadingState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeEditScreen(employeeId: Long, onDone: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel = remember(employeeId) { EmployeeEditViewModel(employeeId, container.employeeRepository) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Karyawan") },
                navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali") } }
            )
        }
    ) { padding ->
        val employee = state.employee
        if (state.isLoading || employee == null) {
            LoadingState(Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        var nik by remember(employee.id) { mutableStateOf(employee.nik) }
        var name by remember(employee.id) { mutableStateOf(employee.name) }
        var phone by remember(employee.id) { mutableStateOf(employee.phone ?: "") }
        var email by remember(employee.id) { mutableStateOf(employee.email ?: "") }
        var address by remember(employee.id) { mutableStateOf(employee.address ?: "") }
        var birthDate by remember(employee.id) { mutableStateOf(employee.birthDate) }
        var gender by remember(employee.id) { mutableStateOf(employee.gender) }
        var joinDate by remember(employee.id) { mutableStateOf<LocalDate?>(employee.joinDate) }
        var resignDate by remember(employee.id) { mutableStateOf(employee.resignDate) }
        var status by remember(employee.id) { mutableStateOf(employee.status) }

        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.errorMessage?.let { ErrorBanner(it, onDismiss = viewModel::clearError) }
            OutlinedTextField(nik, { nik = it }, label = { Text("NIK") }, singleLine = true)
            OutlinedTextField(name, { name = it }, label = { Text("Nama") }, singleLine = true)
            OutlinedTextField(phone, { phone = it }, label = { Text("Telepon") }, singleLine = true)
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
            OutlinedTextField(address, { address = it }, label = { Text("Alamat") })
            DateField("Tanggal lahir", birthDate, { birthDate = it }, allowClear = true)
            EnumDropdownField("Jenis kelamin", gender ?: Gender.MALE, Gender.entries, { gender = it })
            DateField("Tanggal masuk", joinDate, { joinDate = it })
            DateField("Tanggal keluar", resignDate, { resignDate = it }, allowClear = true)
            EnumDropdownField("Status", status, EmployeeStatus.entries, { status = it })

            Button(
                enabled = nik.isNotBlank() && name.isNotBlank() && joinDate != null && !state.isSaving,
                onClick = {
                    viewModel.save(
                        employee.copy(
                            nik = nik.trim(), name = name.trim(),
                            phone = phone.trim().ifBlank { null }, email = email.trim().ifBlank { null },
                            address = address.trim().ifBlank { null }, birthDate = birthDate, gender = gender,
                            joinDate = joinDate!!, resignDate = resignDate, status = status
                        )
                    )
                }
            ) { Text(if (state.isSaving) "Menyimpan..." else "Simpan") }
        }
    }
}
