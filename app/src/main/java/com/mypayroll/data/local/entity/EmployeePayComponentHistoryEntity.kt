package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "employee_pay_component_history",
    foreignKeys = [
        ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PayComponentEntity::class, parentColumns = ["id"], childColumns = ["payComponentId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("employeeId", "effectiveDate"), Index("payComponentId"), Index("referenceDocumentId")]
)
data class EmployeePayComponentHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val payComponentId: Long,
    val effectiveDate: LocalDate,
    val endDate: LocalDate? = null,
    val amount: Long,
    val calculationType: CalculationType,
    val referenceDocumentId: Long? = null,
    val reason: String? = null,
    val notes: String? = null
)
