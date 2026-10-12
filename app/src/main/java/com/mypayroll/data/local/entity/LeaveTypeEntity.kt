package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "leave_type", indices = [Index(value = ["code"], unique = true)])
data class LeaveTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val active: Boolean = true
)
