package com.mypayroll.presentation.payrollsetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class PayrollSetupMenuItem(val title: String, val description: String, val onClick: () -> Unit)

/**
 * Halaman utama Payroll Setup: hub berisi tiga menu (Calendar Hari Kerja & Holiday,
 * Aturan Perhitungan Lembur, Penalty Keterlambatan Bertingkat). Sengaja TIDAK memuat
 * form Gaji Pokok/Tunjangan Jabatan/nominal gaji individual - nilai default komponen
 * gaji (GP/TJ/UM/PH/UL/PL) - fitur "Komponen Gaji Default"/PayComponent sudah dihapus total
 * atas permintaan user, lihat mod report.
 */
@Composable
fun PayrollSetupScreen(
    onBack: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenOvertimeRules: () -> Unit,
    onOpenLatePenalty: () -> Unit,
    onOpenGeneralSettings: () -> Unit,
    onOpenIncentiveTypes: () -> Unit
) {
    val items = listOf(
        PayrollSetupMenuItem(
            "Calendar Hari Kerja & Holiday",
            "Atur status Hari Kerja atau Holiday untuk setiap tanggal",
            onOpenCalendar
        ),
        PayrollSetupMenuItem(
            "Presentase Perhitungan Lembur",
            "Persentase UL untuk lembur 1 jam, 2 jam, dan hari Holiday",
            onOpenOvertimeRules
        ),
        PayrollSetupMenuItem(
            "Penalty Keterlambatan Bertingkat",
            "Nominal penalty kumulatif untuk keterlambatan 5, 15, dan 30 menit",
            onOpenLatePenalty
        ),
        PayrollSetupMenuItem(
            "Pengaturan Umum (Curr/DM/DW/HW)",
            "Currency, Days per Month, Days per Week, Hours per Week - dipakai menurunkan GPPH/GPPJ di Kompensasi",
            onOpenGeneralSettings
        ),
        PayrollSetupMenuItem(
            "Pengaturan Nama Insentif",
            "Daftar nama dan deskripsi insentif yang bisa dipilih saat menambah Insentif di Kompensasi",
            onOpenIncentiveTypes
        )
    )

    com.mypayroll.presentation.common.BackScaffold("Payroll Setup", onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Pengaturan global yang berlaku untuk seluruh karyawan. Nilai gaji pokok, tunjangan, dan " +
                    "nominal individual lainnya tetap diatur lewat data karyawan masing-masing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            items.forEach { item ->
                Card(onClick = item.onClick, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text(item.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
