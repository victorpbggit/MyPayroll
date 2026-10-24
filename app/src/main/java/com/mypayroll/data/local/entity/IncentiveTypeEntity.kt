package com.mypayroll.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Katalog nama insentif ("Pengaturan Nama Insentif" di Payroll Setup) - global, bukan per
 * karyawan/periode. [EmployeeIncentiveEntity.payComponentId] (nama kolom lama, tidak di-rename
 * untuk menghindari rebuild tabel yang tidak perlu) mereferensikan id di sini, tanpa foreign
 * key formal (konsisten dengan kolom itu sejak PayComponent dihapus - lihat mod report).
 */
@Entity(tableName = "incentive_type", indices = [Index(value = ["name"], unique = true)])
data class IncentiveTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null
)
