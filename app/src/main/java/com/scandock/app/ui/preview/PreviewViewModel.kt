package com.scandock.app.ui.preview


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.scandock.app.data.entity.PageEntity
import com.scandock.app.data.entity.ScanEntity
import com.scandock.app.util.PdfUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


data class Page(
    val original: Bitmap,          // never changes
    var current: Bitmap,           // edited bitmap
    val imagePath: String,
    // ---- tool states (per page) ----
    var enhanceMode: EnhanceMode = EnhanceMode.NONE,
    var filterMode: FilterMode = FilterMode.NONE,
    var rotation: Float = 0f,
    var contrast: Float = 1f,
    var sharpness: Float = 0f
)

class PreviewViewModel(
    private val repository: PreviewRepository
) : ViewModel() {

    // ---- PAGES ----
    var pages = mutableStateListOf<Page>()
        private set

    var selectedIndex by mutableStateOf(0)
        private set

    // ---- CAPTURE FLOW ----
    var captureMode by mutableStateOf(CaptureMode.ADD_PAGE)

//    var captureTrigger by mutableStateOf(0)
//        private set

    var hasConsumedCapture by mutableStateOf(false)
        //private set

    fun consumeNewCapture(bitmap: Bitmap) {
        if (hasConsumedCapture) return
        onNewCapture(bitmap)
        hasConsumedCapture = true
    }

    fun triggerNewCapture() {
        hasConsumedCapture = false
        //captureTrigger++
    }

    fun triggerCaptured() {
        hasConsumedCapture = true
    }

    // ---- ADD / RETAKE / REPLACE ----

    fun onNewCapture(bitmap: Bitmap) {
        val originalCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val currentCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val page = Page(
            original = originalCopy,
            current = currentCopy,
            imagePath = ""
        )

        when (captureMode) {
            CaptureMode.RETAKE -> {
                if (pages.isNotEmpty()) {
                    pages[selectedIndex] = page
                } else {
                    pages.add(page)
                    selectedIndex = 0
                }
            }

            CaptureMode.ADD_PAGE -> {
                pages.add(page)
                selectedIndex = pages.lastIndex
            }

            CaptureMode.NONE -> {

            }
        }

        captureMode = CaptureMode.NONE
    }


    // ---- UPDATE CURRENT PAGE CONTENT ----
    fun updateCurrentPage(
        bitmap: Bitmap,
        enhanceMode: EnhanceMode,
        filterMode: FilterMode,
        rotation: Float,
        contrast: Float,
        sharpness: Float
    ) {
        val page = pages[selectedIndex]
        pages[selectedIndex] = page.copy(
            current = bitmap.copy(Bitmap.Config.ARGB_8888, true),
            enhanceMode = enhanceMode,
            filterMode = filterMode,
            rotation = rotation,
            contrast = contrast,
            sharpness = sharpness
        )
    }

    fun updatePageState(
        enhanceMode: EnhanceMode,
        filterMode: FilterMode,
        rotation: Float,
        contrast: Float,
        sharpness: Float
    ) {
        val p = pages[selectedIndex]
        pages[selectedIndex] = p.copy(
            enhanceMode = enhanceMode,
            filterMode = filterMode,
            rotation = rotation,
            contrast = contrast,
            sharpness = sharpness
        )
    }


    // ---- RESET CURRENT PAGE ----
    fun resetCurrentPage() {
        val page = pages[selectedIndex]
        pages[selectedIndex] = page.copy(
            current = page.original.copy(Bitmap.Config.ARGB_8888, true),
            enhanceMode = EnhanceMode.NONE,
            filterMode = FilterMode.NONE,
            rotation = 0f,
            contrast = 1f,
            sharpness = 0f
        )
    }

    // ---- PAGE NAVIGATION ----
    fun selectPage(index: Int) {
        selectedIndex = index.coerceIn(0, pages.lastIndex)
    }

    fun removePage(index: Int) {
        pages.removeAt(index)
        selectedIndex = selectedIndex
            .coerceAtMost(pages.lastIndex)
            .coerceAtLeast(0)
    }

    fun clear() {
        pages.clear()
        selectedIndex = 0
    }

    // ---- NAV HELPERS ----
    fun onRetake(navController: NavController) {
        navController.popBackStack()
    }

    fun onAccept(
        uri: Uri,
        onFinish: (Uri) -> Unit
    ) {
        //onFinish(uri)
    }


     fun persistScan(context: Context, onResult: (Boolean)->Unit) {
         viewModelScope.launch(Dispatchers.IO) {
             try {
                 val timestamp = System.currentTimeMillis()
                 val scanDir = File(context.filesDir, "scans/scan_$timestamp")
                 scanDir.mkdirs()

                 val pageEntities = mutableListOf<PageEntity>()
                 val bitmapsForPdf = mutableListOf<Bitmap>()

                 pages.forEachIndexed { index, page ->
                     val imageFile = File(scanDir, "page_$index.png")
                     imageFile.outputStream().use {
                         page.current.compress(Bitmap.CompressFormat.PNG, 100, it)
                     }

                     bitmapsForPdf.add(page.current)

                     pageEntities.add(
                         PageEntity(
                             scanId = 0L,
                             imagePath = imageFile.absolutePath,
                             enhanceMode = page.enhanceMode,
                             filterMode = page.filterMode,
                             rotation = page.rotation,
                             contrast = page.contrast,
                             sharpness = page.sharpness,
                             orderIndex = index
                         )
                     )
                 }

                 val pdfFile = File(scanDir, "output.pdf")
                 PdfUtil.writePdf(bitmapsForPdf, pdfFile)

                 val scanEntity = ScanEntity(
                     title = "Scan $timestamp",
                     folderPath = scanDir.absolutePath,
                     pdfPath = pdfFile.absolutePath
                 )

                 repository.saveScan(scanEntity, pageEntities)
                 withContext(Dispatchers.Main) {
                     onResult(true)
                 }
             }catch (e: Exception){
                 Log.e("TAG", "persistScan: $e")
                 e.printStackTrace()
                 withContext(Dispatchers.Main) {
                     onResult(false)
                 }
             }
        }
    }

    suspend fun loadScan(scanId: Long) {
        val pageEntities = repository.getPagesForScan(scanId)

        pages.clear()
        pageEntities
            .sortedBy { it.orderIndex }
            .forEach { pe ->
                val bmp = BitmapFactory.decodeFile(pe.imagePath)
                pages.add(
                    Page(
                        original = bmp.copy(Bitmap.Config.ARGB_8888, true),
                        current = bmp.copy(Bitmap.Config.ARGB_8888, true),
                        imagePath = pe.imagePath,
                        enhanceMode = pe.enhanceMode,
                        filterMode = pe.filterMode,
                        rotation = pe.rotation,
                        contrast = pe.contrast,
                        sharpness = pe.sharpness
                    )
                )
            }
    }



}

