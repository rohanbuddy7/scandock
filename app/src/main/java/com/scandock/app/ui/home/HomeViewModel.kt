package com.scandock.app.ui.home

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scandock.app.data.dao.ScanDao
import com.scandock.app.data.entity.ScanEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    val scans = repository.getAllScans()

    fun delete(scanEntity: ScanEntity){
        viewModelScope.launch {
            repository.delete(scanEntity = scanEntity)
        }
    }

    fun sharePdf(context: Context, scan: ScanEntity) {
        viewModelScope.launch {
            val pdfFile = File(scan.pdfPath)

            Log.e("PDF_SHARE", "PDF Path = ${scan.pdfPath}")
            Log.e("PDF_SHARE", "Exists = ${File(scan.pdfPath).exists()}")

            if (!pdfFile.exists()) return@launch

            val uri = FileProvider.getUriForFile(
                context,
                "com.scandock.app.provider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(shareIntent, "Share PDF")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }


}
