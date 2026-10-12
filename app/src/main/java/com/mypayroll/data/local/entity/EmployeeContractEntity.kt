package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "employee_contract",
    foreignKeys = [
        ForeignKey(entity = EmployeeEntity::class, parentColumns = ["id"], childColumns = ["employeeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ContractTypeEntity::class, parentColumns = ["id"], childColumns = ["contractTypeId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = DocumentEntity::class, parentColumns = ["id"], childColumns = ["documentId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index(value = ["contractNumber"], unique = true), Index("employeeId", "startDate"), Index("contractTypeId"), Index("documentId")]
)
data class EmployeeContractEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val contractNumber: String,
    val contractTypeId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val documentId: Long? = null,
    val status: ContractStatus = ContractStatus.ACTIVE,
    val terminationDate: LocalDate? = null,
    val terminationReason: String? = null,
    val notes: String? = null
)
