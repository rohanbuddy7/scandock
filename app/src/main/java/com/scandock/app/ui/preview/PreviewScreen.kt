package com.scandock.app.ui.preview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.scandock.app.ui.nav.ScanRoutes
import com.scandock.app.ui.scanner.CameraOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min
import android.graphics.Color as AndroidColor

enum class EnhanceMode {
    NONE, CONTRAST, SHARP, DOC
}

enum class FilterMode {
    NONE, GRAY, BW, WARM, COOL
}

enum class Tool {
    NONE, CROP, ENHANCE, FILTER
}

enum class CaptureMode {
    NONE,
    RETAKE,
    ADD_PAGE
}


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun PreviewScreen(
    navController: NavController,
    onFinish: (Uri) -> Unit,
    backStackEntry: NavBackStackEntry
) {

    val context = LocalContext.current
    val entry = navController.getBackStackEntry(ScanRoutes.scan_graph)

    val viewModel: PreviewViewModel = viewModel(
        entry,
        factory = PreviewViewModelFactory(context)
    )

    val from = backStackEntry.arguments?.getString("from")
    val args = navController.currentBackStackEntry?.arguments;
    val uriString = args?.getString("uri")
    val scanId = args?.getLong("scanId")?.takeIf { it != -1L }

    val isCameraUri =
        uriString != null &&
                (uriString.startsWith("content://") || uriString.startsWith("file://"))

    // Case 1: New scan from Camera (URI)
    if (isCameraUri) {
        val uri = Uri.parse(uriString)

        ScanPreviewScreen(
            from = from,
            viewModel = viewModel,
            navController = navController,
            imageUri = uri,
            onRetake = {
                viewModel.onRetake(navController)
            },
            onAddPage = {
                if(from == ScanRoutes.CAMERA) {
                    navController.navigate(ScanRoutes.CAMERA) {
                        launchSingleTop = true
                    }
                }
            },
            onFinish = {
                //viewModel.onAccept(uri, onFinish)
                navController.navigate(ScanRoutes.HOME) {
                    popUpTo(ScanRoutes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        return
    }

    // Case 2: Existing scan from Home (scanId)
    else if (scanId != null) {
        viewModel.triggerCaptured()

        LaunchedEffect(scanId) {
            if (viewModel.pages.isEmpty()) {
                viewModel.loadScan(scanId)
            }
        }

        val page = viewModel.pages.firstOrNull()
        if (page != null && page.imagePath.isNotEmpty()) {

            val imageUri = Uri.fromFile(File(page.imagePath))

            ScanPreviewScreen(
                from = from,
                viewModel = viewModel,
                navController = navController,
                imageUri = imageUri,
                onRetake = {
                    navController.popBackStack()
                },
                onAddPage = {
                    if(from == ScanRoutes.HOME){
                        navController.navigate(ScanRoutes.CAMERA) {
                            launchSingleTop = true
                        }
                    }
                },
                onFinish = {
                    //viewModel.onAccept("", onFinish)
                    navController.navigate(ScanRoutes.HOME) {
                        popUpTo(ScanRoutes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }

}





@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ScanPreviewScreen(
    from: String?,
    viewModel: PreviewViewModel,
    navController: NavController,
    imageUri: Uri,
    onRetake: () -> Unit,
    onAddPage: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current

    // ---- CAPTURE ENTRY ----
    //LaunchedEffect(imageUri, viewModel.captureTrigger) {
    LaunchedEffect(imageUri) {
        if (!viewModel.hasConsumedCapture) {
            val bitmap = ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, imageUri)
            ).copy(Bitmap.Config.ARGB_8888, true)

            viewModel.consumeNewCapture(bitmap)
        }
    }


    if (viewModel.pages.isEmpty()) return
    val page = viewModel.pages[viewModel.selectedIndex]
    val pageIndex = viewModel.selectedIndex
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var enhanceMode by remember(viewModel.selectedIndex) { mutableStateOf(page.enhanceMode) }
    var filterMode by remember(viewModel.selectedIndex) { mutableStateOf(page.filterMode) }
    var rotation by remember(viewModel.selectedIndex) { mutableStateOf(page.rotation) }
    var contrast by remember(viewModel.selectedIndex) { mutableStateOf(page.contrast) }
    var sharpness by remember(viewModel.selectedIndex) { mutableStateOf(page.sharpness) }

    // ---- UI-ONLY STATE ----
    var sharpnessTemp by remember { mutableStateOf(sharpness) }
    var activeTool by remember { mutableStateOf(Tool.NONE) }
    var isProcessing by remember { mutableStateOf(false) }

    // ---- PREVIEW PIPELINE ----
    val processedBitmap by produceState(
        initialValue = page.current,
        page.current,
        enhanceMode,
        filterMode,
        contrast,
        sharpness
    ) {
        isProcessing = true

        value = withContext(Dispatchers.Default) {
            var bmp = page.current

            if (enhanceMode != EnhanceMode.NONE) {
                bmp = when (enhanceMode) {
                    EnhanceMode.CONTRAST -> adjustContrast(bmp, contrast)
                    EnhanceMode.SHARP -> sharpenFast(bmp, sharpness)
                    EnhanceMode.DOC -> docBoost(bmp)
                    else -> bmp
                }
            }

            if (filterMode != FilterMode.NONE) {
                bmp = when (filterMode) {
                    FilterMode.GRAY -> toGray(bmp)
                    FilterMode.BW -> toBW(bmp)
                    FilterMode.WARM -> warm(bmp)
                    FilterMode.COOL -> cool(bmp)
                    else -> bmp
                }
            }

            bmp
        }

        isProcessing = false
    }


    Column(modifier = Modifier.fillMaxSize()) {

        // ================= IMAGE =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { activeTool = Tool.NONE },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = processedBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation }
            )

            if (isProcessing || isSaving) {
                CircularProgressIndicator(color = Color.White)
            }

        }

        // ================= BOTTOM PANEL =================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.85f)
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ---------- THUMBNAILS ----------
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    itemsIndexed(viewModel.pages) { index, p ->
                        val selected = index == viewModel.selectedIndex

                        Image(
                            bitmap = p.current.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    if (selected) 2.dp else 0.dp,
                                    Color.White,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    viewModel.updatePageState(
                                        enhanceMode = enhanceMode,
                                        filterMode = filterMode,
                                        rotation = rotation,
                                        contrast = contrast,
                                        sharpness = sharpness
                                    )
                                    viewModel.selectPage(index)
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Text(
                    text = "${viewModel.pages.size} pages",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ---------- SUB OPTIONS ----------
                when (activeTool) {
                    Tool.CROP -> {

                    }

                    Tool.ENHANCE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Text("Contrast", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = contrast,
                                onValueChange = {
                                    contrast = it
                                    enhanceMode = EnhanceMode.CONTRAST
                                },
                                valueRange = 0.8f..2f
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Sharpness", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = sharpnessTemp,
                                onValueChange = { sharpnessTemp = it },
                                onValueChangeFinished = {
                                    sharpness = sharpnessTemp
                                    enhanceMode = EnhanceMode.SHARP
                                }
                            )
                        }
                    }

                    Tool.FILTER -> {
                        SubOptionsRow {
                            item { SubOption("None") { filterMode = FilterMode.NONE }}
                            item { SubOption("Gray") { filterMode = FilterMode.GRAY }}
                            item { SubOption("B&W") { filterMode = FilterMode.BW }}
                            item { SubOption("Warm") { filterMode = FilterMode.WARM }}
                            item { SubOption("Cool") { filterMode = FilterMode.COOL }}
                        }
                    }

                    Tool.NONE -> Unit
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ---------- TOOLS ----------
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    item {
                        CameraOption("Crop", Icons.Outlined.Crop) {
                            viewModel.captureMode = CaptureMode.RETAKE
//                            viewModel.updateCurrentPage(
//                                bitmap = page.current,
//                                enhanceMode = enhanceMode,
//                                filterMode = filterMode,
//                                rotation = rotation,
//                                contrast = contrast,
//                                sharpness = sharpness
//                            )
//                            scope.launch {
//                                delay(1000)
//                                navController.navigate("crop")
//                            }
                            navController.navigate("crop")
                            Log.e("TAG", "ScanPreviewScreen yo we have pages: ${viewModel.pages.size}")
                        }
                    }
                    item {
                        CameraOption("Enhance", Icons.Outlined.Tune) {
                            activeTool = Tool.ENHANCE
                        }
                    }
                    item {
                        CameraOption("Filter", Icons.Outlined.AutoFixHigh) {
                            activeTool = Tool.FILTER
                        }
                    }
                    item {
                        CameraOption("Rotate", Icons.Outlined.RotateRight) {
                            rotation += 90f
                        }
                    }
                    item {
                        CameraOption("Reset", Icons.Outlined.Refresh) {
                            viewModel.updateCurrentPage(
                                bitmap = page.original,
                                enhanceMode = enhanceMode,
                                filterMode = filterMode,
                                rotation = rotation,
                                contrast = contrast,
                                sharpness = sharpness
                            )


                            enhanceMode = EnhanceMode.NONE
                            filterMode = FilterMode.NONE
                            rotation = 0f
                            contrast = 1f
                            sharpness = 0f

                            viewModel.resetCurrentPage()
                        }
                    }
                    item {
                        CameraOption("Delete", Icons.Outlined.DeleteOutline) {
                            viewModel.removePage(viewModel.selectedIndex)
                            if (viewModel.pages.isEmpty()) onRetake()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ---------- ACTIONS ----------
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    if(from== ScanRoutes.CAMERA) {
                        OutlinedButton(onClick = {
                            viewModel.captureMode = CaptureMode.RETAKE
                            viewModel.triggerNewCapture()
                            onRetake()
                        }) { Text("Retake") }
                    }

                    OutlinedButton(onClick = {
                        viewModel.captureMode = CaptureMode.ADD_PAGE
                        viewModel.triggerNewCapture()
                        onAddPage()
                    }) { Text("Add page") }

                    Button(
                        enabled = !isSaving,
                        onClick = {
                            isSaving = true
                            viewModel.updateCurrentPage(
                                bitmap = page.current,
                                enhanceMode = enhanceMode,
                                filterMode = filterMode,
                                rotation = rotation,
                                contrast = contrast,
                                sharpness = sharpness
                            )
                            viewModel.persistScan(context, { success ->
                                isSaving = false
                                if (success){
                                    Toast.makeText(context, "Successfully saved", Toast.LENGTH_SHORT).show()
                                    onFinish()
                                } else {
                                    Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }) {
                        Text("Save a copy", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}




@Composable
fun SubOptionsRow(
    content: LazyListScope.() -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
        content = content
    )
}


@Composable
fun SubOption(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp
        )
    }
}

fun adjustContrast(src: Bitmap, value: Float): Bitmap {
    val scale = value
    val translate = (-.5f * scale + .5f) * 255f

    val cm = ColorMatrix(floatArrayOf(
        scale,0f,0f,0f,translate,
        0f,scale,0f,0f,translate,
        0f,0f,scale,0f,translate,
        0f,0f,0f,1f,0f
    ))

    return applyColorMatrix(src, cm)
}

fun sharpenFast(src: Bitmap, strength: Float): Bitmap {
    if (strength == 0f) return src

    val scale = 0.5f
    val small = Bitmap.createScaledBitmap(
        src,
        (src.width * scale).toInt(),
        (src.height * scale).toInt(),
        true
    )

    val center = 5f + strength * 2f
    val kernel = arrayOf(
        floatArrayOf(0f, -1f, 0f),
        floatArrayOf(-1f, center, -1f),
        floatArrayOf(0f, -1f, 0f)
    )

    val sharpenedSmall = convolution(small, kernel)

    return Bitmap.createScaledBitmap(
        sharpenedSmall,
        src.width,
        src.height,
        true
    )
}


fun convolution(src: Bitmap, kernel: Array<FloatArray>): Bitmap {
    val width = src.width
    val height = src.height

    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val kSize = kernel.size
    val kOffset = kSize / 2

    for (y in kOffset until height - kOffset) {
        for (x in kOffset until width - kOffset) {

            var r = 0f
            var g = 0f
            var b = 0f

            for (ky in 0 until kSize) {
                for (kx in 0 until kSize) {
                    val px = x + kx - kOffset
                    val py = y + ky - kOffset

                    val pixel = src.getPixel(px, py)
                    val weight = kernel[ky][kx]

                    r += AndroidColor.red(pixel) * weight
                    g += AndroidColor.green(pixel) * weight
                    b += AndroidColor.blue(pixel) * weight
                }
            }

            output.setPixel(
                x, y,
                AndroidColor.argb(
                    255,
                    r.coerceIn(0f, 255f).toInt(),
                    g.coerceIn(0f, 255f).toInt(),
                    b.coerceIn(0f, 255f).toInt()
                )
            )
        }
    }

    return output
}




fun docBoost(src: Bitmap) =
    applyColorMatrix(src, ColorMatrix().apply {
        setSaturation(0f)
        postConcat(ColorMatrix(floatArrayOf(
            1.8f,0f,0f,0f,-80f,
            0f,1.8f,0f,0f,-80f,
            0f,0f,1.8f,0f,-80f,
            0f,0f,0f,1f,0f
        )))
    })

fun toGray(src: Bitmap) =
    applyColorMatrix(src, ColorMatrix().apply {
        setSaturation(0f)
    })

fun toBW(src: Bitmap) =
    applyColorMatrix(src, ColorMatrix().apply {
        setSaturation(0f)
        postConcat(ColorMatrix(floatArrayOf(
            2f,0f,0f,0f,-255f,
            0f,2f,0f,0f,-255f,
            0f,0f,2f,0f,-255f,
            0f,0f,0f,1f,0f
        )))
    })

fun warm(src: Bitmap) =
    applyColorMatrix(src, ColorMatrix(floatArrayOf(
        1.1f,0f,0f,0f,20f,
        0f,1f,0f,0f,0f,
        0f,0f,0.9f,0f,-10f,
        0f,0f,0f,1f,0f
    )))

fun cool(src: Bitmap) =
    applyColorMatrix(src, ColorMatrix(floatArrayOf(
        0.9f,0f,0f,0f,-10f,
        0f,1f,0f,0f,0f,
        0f,0f,1.1f,0f,20f,
        0f,0f,0f,1f,0f
    )))

fun applyColorMatrix(src: Bitmap, cm: ColorMatrix): Bitmap {
    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(cm)
    }
    canvas.drawBitmap(src, 0f, 0f, paint)
    return result
}

