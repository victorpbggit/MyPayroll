package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "employee_incentive",
    foreignKeys = [
        ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("employeeId", "date"), Index("payComponentId"), Index("workOrderId")]
)
data class EmployeeIncentiveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val date: LocalDate,
    /** Sebelumnya foreign key ke PayComponentEntity (kini dihapus) - kolom dibiarkan ada tanpa relasi, lihat mod report. */
    val payComponentId: Long,
    val amount: Long,
    val workOrderId: Long? = null,
    val referenceId: Long? = null,
    val reason: String? = null,
    val approvedBy: Long? = null,
    val notes: String? = null
)
