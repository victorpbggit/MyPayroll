package com.mypayroll.data.local.entity
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "document", indices = [Index("documentType")])
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val documentType: String,
    val storagePath: String,
    val createdAt: LocalDateTime
)
