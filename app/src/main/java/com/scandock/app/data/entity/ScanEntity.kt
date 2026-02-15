package com.scandock.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val folderPath: String,
    val pdfPath: String,
    val createdAt: Long = System.currentTimeMillis()
)
