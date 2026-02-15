package com.scandock.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.scandock.app.ui.preview.EnhanceMode
import com.scandock.app.ui.preview.FilterMode

@Entity(
    tableName = "pages",
    foreignKeys = [
        ForeignKey(
            entity = PageEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scanId: Long,
    val imagePath: String,

    val enhanceMode: EnhanceMode,
    val filterMode: FilterMode,
    val rotation: Float,
    val contrast: Float,
    val sharpness: Float,
    val orderIndex: Int
)
