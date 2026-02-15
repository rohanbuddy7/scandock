package com.scandock.app.aidl

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.scandock.app.provider.ScanDockProvider
import java.io.File

fun CaptureAndReturn(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    val scanFolder = File(context.filesDir, "scans")
    scanFolder.mkdirs()
    val file = File(scanFolder, "scan_${System.currentTimeMillis()}.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                //val uri = ScanDockProvider.getUriForFile(file)
                val uri = FileProvider.getUriForFile(
                    context,
                    "com.scandock.app.provider",
                    file
                )
                onImageCaptured(uri)
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "Scan failed", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
