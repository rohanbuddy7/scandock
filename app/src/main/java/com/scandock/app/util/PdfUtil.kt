package com.scandock.app.util

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import java.io.File


object PdfUtil {

    fun writePdf(bitmaps: List<Bitmap>, file: File) {
        val document = PdfDocument()

        bitmaps.forEachIndexed { index, bmp ->
            val pageInfo = PdfDocument.PageInfo.Builder(
                bmp.width, bmp.height, index + 1
            ).create()

            val page = document.startPage(pageInfo)
            page.canvas.drawBitmap(bmp, 0f, 0f, null)
            document.finishPage(page)
        }

        file.outputStream().use { document.writeTo(it) }
        document.close()
    }
}
