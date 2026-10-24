package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "employee_salary_history",
    foreignKeys = [ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("employeeId", "effectiveDate"), Index("referenceDocumentId"), Index("approvedBy")]
)
data class EmployeeSalaryHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val effectiveDate: LocalDate,
    val basicSalary: Long,
    val salaryType: SalaryType = SalaryType.MONTHLY,
    /** Tunjangan Jabatan (TJ) - hanya berlaku saat employee.payrollType == MONTHLY; 0 jika tidak berlaku. */
    val positionAllowance: Long = 0L,
    val reason: String? = null,
    val referenceDocumentId: Long? = null,
    val approvedBy: Long? = null,
    val notes: String? = null
)
