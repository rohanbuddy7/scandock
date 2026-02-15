package com.scandock.app.ui.scanner

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.scandock.app.aidl.CaptureAndReturn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp


@Composable
fun DocScannerScreen(
    modifier: Modifier = Modifier,
    onImageCaptured: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ---- STATE ----
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var timerSeconds by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(0) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // ---- CAMERA BIND ----
    LaunchedEffect(cameraSelector) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture.flashMode =
            if (isFlashOn) ImageCapture.FLASH_MODE_ON
            else ImageCapture.FLASH_MODE_OFF

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            kotlinx.coroutines.delay(1000)
            countdown--
        } else if (countdown == 0 && timerSeconds > 0) {
            CaptureAndReturn(
                context,
                imageCapture,
                onImageCaptured
            )
            timerSeconds = 0
        }
    }


    Column(modifier = modifier.fillMaxSize()) {

        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        // ---- BOTTOM CONTROLS ----
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.85f)
        ) {
            Column(
                modifier = Modifier.navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(30.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // FLASH
                    item {
                        CameraOption(
                            label = if (isFlashOn) "Flash On" else "Flash Off",
                            icon = if (isFlashOn)
                                Icons.Outlined.FlashOn
                            else
                                Icons.Outlined.FlashOff
                        ) {
                            isFlashOn = !isFlashOn
                            imageCapture.flashMode =
                                if (isFlashOn)
                                    ImageCapture.FLASH_MODE_ON
                                else
                                    ImageCapture.FLASH_MODE_OFF
                        }
                    }

                    // FLIP CAMERA
                    item {
                        CameraOption(
                            label = "Flip",
                            icon = Icons.Outlined.Cameraswitch
                        ) {
                            cameraSelector =
                                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                else
                                    CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    }

                    // TIMER
                    item {
                        CameraOption(
                            label = "Timer",
                            icon = Icons.Outlined.Timer
                        ) {
                            timerSeconds =
                                if (timerSeconds == 0) 3 else 0 // toggle 3s
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ---- SHUTTER ----
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            if (timerSeconds > 0) {
                                countdown = timerSeconds
                            } else {
                                CaptureAndReturn(
                                    context,
                                    imageCapture,
                                    onImageCaptured
                                )
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                when {
                    countdown > 0 -> {
                        Text(
                            text = "${countdown}",
                            color = Color.White,
                            fontSize = 22.sp
                        )
                    }
                    timerSeconds > 0 -> {
                        Text(
                            text = "Timer: ${timerSeconds}s",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

            }
        }
    }
}


@Composable
fun CameraOption(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
