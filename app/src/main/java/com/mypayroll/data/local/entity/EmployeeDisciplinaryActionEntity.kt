package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "employee_disciplinary_action",
    foreignKeys = [ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("employeeId", "incidentDate"), Index("referenceDocumentId"), Index("issuedBy")]
)
data class EmployeeDisciplinaryActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val incidentDate: LocalDate,
    val actionDate: LocalDate,
    val actionType: DisciplinaryActionType,
    val category: String? = null,
    val description: String,
    val referenceDocumentId: Long? = null,
    val issuedBy: Long? = null,
    val validUntil: LocalDate? = null,
    val notes: String? = null
)
