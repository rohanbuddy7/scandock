package com.scandock.app.ui.preview

import android.util.Log
import androidx.room.Transaction
import com.scandock.app.data.dao.PageDao
import com.scandock.app.data.dao.ScanDao
import com.scandock.app.data.entity.PageEntity
import com.scandock.app.data.entity.ScanEntity
import kotlinx.coroutines.flow.Flow

class PreviewRepository(
    private val scanDao: ScanDao,
    private val pageDao: PageDao
) {

    @Transaction
    suspend fun saveScan(
        scan: ScanEntity,
        pages: List<PageEntity>
    ) {
        pageDao.deletePagesForScan(0)

        val scanId = if(scan.id == 0L){
            scanDao.insert(scan)
        } else {
            scanDao.update(scan)
            scan.id
        }
        //val realScanId = scanDao.insert(scan)

        val fixedPages = pages.map { page->
            page.copy(scanId = scanId)
        }

        pageDao.deletePagesForScan(scanId)
        pageDao.insertAll(fixedPages)

    }

    suspend fun getPagesForScan(scanId: Long): List<PageEntity> {
        return pageDao.getPagesForScan(scanId)
    }

    fun getAllScans(): Flow<List<ScanEntity>> =
        scanDao.getAllScans()

    suspend fun updateScan(
        scanEntity: ScanEntity,
        pages: List<PageEntity>
    ){
        scanDao.update(scanEntity)
    }

}
