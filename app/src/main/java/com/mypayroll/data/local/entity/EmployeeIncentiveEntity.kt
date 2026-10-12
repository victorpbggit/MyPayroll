package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "employee_incentive",
    foreignKeys = [
        ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PayComponentEntity::class, parentColumns = ["id"], childColumns = ["payComponentId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("employeeId", "date"), Index("payComponentId"), Index("workOrderId")]
)
data class EmployeeIncentiveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val date: LocalDate,
    val payComponentId: Long,
    val amount: Long,
    val workOrderId: Long? = null,
    val referenceId: Long? = null,
    val reason: String? = null,
    val approvedBy: Long? = null,
    val notes: String? = null
)
