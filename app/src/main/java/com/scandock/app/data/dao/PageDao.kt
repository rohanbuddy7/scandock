package com.scandock.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.scandock.app.data.entity.PageEntity

@Dao
interface PageDao {
    @Query("""
        SELECT * FROM pages 
        WHERE scanId = :scanId 
        ORDER BY orderIndex ASC
    """)
    suspend fun getPagesForScan(scanId: Long): List<PageEntity>

    @Insert
    suspend fun insertAll(pages: List<PageEntity>)

    @Delete
    suspend fun delete(page: PageEntity)

    @Query("DELETE FROM pages WHERE scanId = :scanId")
    suspend fun deletePagesForScan(scanId: Long)

}
