package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pay_component", indices = [Index(value = ["code"], unique = true)])
data class PayComponentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val category: PayComponentCategory,
    val calculationType: CalculationType,
    val taxable: Boolean = true,
    val bpjsApplicable: Boolean = true,
    val active: Boolean = true
)
