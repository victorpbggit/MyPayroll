package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "employee_position_history",
    foreignKeys = [
        ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PositionEntity::class, parentColumns = ["id"], childColumns = ["positionId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = DepartmentEntity::class, parentColumns = ["id"], childColumns = ["departmentId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["supervisorId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("employeeId", "startDate"), Index("positionId"), Index("departmentId"), Index("supervisorId")]
)
data class EmployeePositionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val positionId: Long,
    val departmentId: Long,
    val supervisorId: Long? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val reason: String? = null,
    val notes: String? = null
)
