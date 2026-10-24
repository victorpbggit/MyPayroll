package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "employee", indices = [Index(value = ["nik"], unique = true), Index(value = ["name"])])
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nik: String,
    val name: String,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val joinDate: LocalDate,
    val resignDate: LocalDate? = null,
    val status: EmployeeStatus = EmployeeStatus.ACTIVE,
    val payrollType: PayrollType? = null,
    val memberType: MemberType? = null,
    val emergencyContact: String? = null,
    /** Upah Lembur (UL) per jam - nullable; saat null, UI menampilkan GPPH sebagai default (belum disimpan secara eksplisit). */
    val overtimeRate: Long? = null,
    /** Premi Lembur (PL) - nullable; saat null, UI menampilkan GPPH sebagai default (belum disimpan secara eksplisit). */
    val overtimePremium: Long? = null
)
