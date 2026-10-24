package com.mypayroll.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Riwayat Gaji Variable (GV) per karyawan, berlaku efektif per tanggal - pola yang sama dengan
 * [EmployeeSalaryHistoryEntity]. GV sendiri tidak disimpan sebagai satu kolom; GV = uangMakan +
 * uangTransport + uangHadir, dihitung di layer domain/presentation, bukan didenormalisasi di sini.
 */
@Entity(
    tableName = "employee_variable_salary_history",
    foreignKeys = [ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("employeeId", "effectiveDate")]
)
data class EmployeeVariableSalaryHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val effectiveDate: LocalDate,
    val uangMakan: Long,
    val uangTransport: Long,
    val uangHadir: Long,
    val reason: String? = null
)
