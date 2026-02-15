package com.scandock.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.scandock.app.data.dao.PageDao
import com.scandock.app.data.dao.ScanDao
import com.scandock.app.data.entity.PageEntity
import com.scandock.app.data.entity.ScanEntity


@Database(
    entities = [ScanEntity::class, PageEntity::class],
    version = 5
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun pageDao(): PageDao
}
