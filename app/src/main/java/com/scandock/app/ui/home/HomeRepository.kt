package com.scandock.app.ui.home

import com.scandock.app.data.dao.ScanDao
import com.scandock.app.data.entity.ScanEntity
import kotlinx.coroutines.flow.Flow

class HomeRepository(
    private val scanDao: ScanDao
) {
    fun getAllScans(): Flow<List<ScanEntity>> =
        scanDao.getAllScans()

    suspend fun delete(scanEntity: ScanEntity) = scanDao.delete(scan = scanEntity)
}
