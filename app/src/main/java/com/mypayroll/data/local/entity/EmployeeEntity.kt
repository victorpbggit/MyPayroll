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
    val status: EmployeeStatus = EmployeeStatus.ACTIVE
)
