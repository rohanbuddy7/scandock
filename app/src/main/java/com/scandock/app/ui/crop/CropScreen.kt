package com.scandock.app.ui.crop

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.scandock.app.ui.nav.ScanRoutes
import com.scandock.app.ui.preview.PreviewViewModel
import com.scandock.app.ui.preview.PreviewViewModelFactory


enum class Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }


@Composable
fun CropScreen(
    navController: NavController,
) {

    val context = LocalContext.current
    val entry = navController.getBackStackEntry(ScanRoutes.scan_graph)

    val viewModel: PreviewViewModel = viewModel(
        entry,
        factory = PreviewViewModelFactory(context)
    )
    Log.e("TAG", "ScanPreviewScreen yo we have pages from crop: ${viewModel.pages.size}")
    if (viewModel.pages.isEmpty()) return
    val pages = viewModel.pages
    val selectedIndex = viewModel.selectedIndex
    val page = pages[selectedIndex]


    CropUI(
        bitmap = page.current,   //  use current bitmap
        onCancel = {
            navController.popBackStack()
        },
        onDone = { croppedBitmap ->
            //  update only current bitmap, keep original safe
            viewModel.updateCurrentPage(
                bitmap = croppedBitmap,
                enhanceMode = page.enhanceMode,
                filterMode = page.filterMode,
                rotation = page.rotation,
                contrast = page.contrast,
                sharpness = page.sharpness
            )

            navController.popBackStack()
            Log.d("CropScreen", "Crop applied to page $selectedIndex")
        }
    )
}


@Composable
fun CropUI(
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onDone: (Bitmap) -> Unit
) {
    var imageSize by remember { mutableStateOf(Size.Zero) }

    var cropRect by remember {
        mutableStateOf(RectF(200f, 300f, 800f, 900f))
    }

    var activeCorner by remember { mutableStateOf<Corner?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // IMAGE
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    imageSize = it.size.toSize()
                }
        )

        // OVERLAY
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            activeCorner = detectCorner(offset, cropRect)
                        },
                        onDrag = { change, drag ->
                            change.consume()

                            cropRect = when (activeCorner) {
                                Corner.TOP_LEFT ->
                                    RectF(
                                        cropRect.left + drag.x,
                                        cropRect.top + drag.y,
                                        cropRect.right,
                                        cropRect.bottom
                                    )

                                Corner.TOP_RIGHT ->
                                    RectF(
                                        cropRect.left,
                                        cropRect.top + drag.y,
                                        cropRect.right + drag.x,
                                        cropRect.bottom
                                    )

                                Corner.BOTTOM_LEFT ->
                                    RectF(
                                        cropRect.left + drag.x,
                                        cropRect.top,
                                        cropRect.right,
                                        cropRect.bottom + drag.y
                                    )

                                Corner.BOTTOM_RIGHT ->
                                    RectF(
                                        cropRect.left,
                                        cropRect.top,
                                        cropRect.right + drag.x,
                                        cropRect.bottom + drag.y
                                    )

                                null -> // drag inside → move whole rect
                                    RectF(
                                        cropRect.left + drag.x,
                                        cropRect.top + drag.y,
                                        cropRect.right + drag.x,
                                        cropRect.bottom + drag.y
                                    )
                            }.clampTo(imageSize)
                        },
                        onDragEnd = { activeCorner = null }
                    )
                }
        ) {

            drawContext.canvas.saveLayer(
                androidx.compose.ui.geometry.Rect(
                    0f,
                    0f,
                    size.width,
                    size.height
                ),
                Paint()
            )

            // DARK MASK
            drawRect(Color.Black.copy(alpha = 0.6f))

            // CLEAR HOLE
            drawRect(
                Color.Transparent,
                topLeft = Offset(cropRect.left, cropRect.top),
                size = Size(cropRect.width(), cropRect.height()),
                blendMode = BlendMode.Clear
            )

            drawContext.canvas.restore()

            // BORDER
            drawRect(
                Color.White,
                topLeft = Offset(cropRect.left, cropRect.top),
                size = Size(cropRect.width(), cropRect.height()),
                style = Stroke(15f)
            )

            drawHandles(cropRect)
        }

        // ACTIONS
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
            Button(onClick = {
                onDone(cropFromViewRect(bitmap, cropRect, imageSize))
            }) {
                Text("Done")
            }
        }
    }
}

fun detectCorner(touch: Offset, rect: RectF): Corner? {
    val threshold = 70f

    return when {
        distance(touch, Offset(rect.left, rect.top)) < threshold -> Corner.TOP_LEFT
        distance(touch, Offset(rect.right, rect.top)) < threshold -> Corner.TOP_RIGHT
        distance(touch, Offset(rect.left, rect.bottom)) < threshold -> Corner.BOTTOM_LEFT
        distance(touch, Offset(rect.right, rect.bottom)) < threshold -> Corner.BOTTOM_RIGHT
        else -> null
    }
}


fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHandles(rect: RectF) {
    val r = 27f
    drawCircle(Color.White, r, Offset(rect.left, rect.top))
    drawCircle(Color.White, r, Offset(rect.right, rect.top))
    drawCircle(Color.White, r, Offset(rect.left, rect.bottom))
    drawCircle(Color.White, r, Offset(rect.right, rect.bottom))
}

fun RectF.clampTo(size: Size): RectF {
    val minSize = 100f

    val left = left.coerceIn(0f, size.width - minSize)
    val top = top.coerceIn(0f, size.height - minSize)
    val right = right.coerceIn(left + minSize, size.width)
    val bottom = bottom.coerceIn(top + minSize, size.height)

    return RectF(left, top, right, bottom)
}

fun distance(a: Offset, b: Offset): Float =
    kotlin.math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))


//fun cropFromViewRect(
//    bitmap: Bitmap,
//    rect: RectF,
//    viewSize: Size
//): Bitmap {
//    val scaleX = bitmap.width / viewSize.width
//    val scaleY = bitmap.height / viewSize.height
//
//    val x = (rect.left * scaleX).toInt()
//    val y = (rect.top * scaleY).toInt()
//    val w = (rect.width() * scaleX).toInt()
//    val h = (rect.height() * scaleY).toInt()
//
//    return Bitmap.createBitmap(bitmap, x, y, w, h)
//}

fun cropFromViewRect(
    bitmap: Bitmap,
    cropRect: RectF,
    viewSize: Size
): Bitmap {

    val imageRect = calculateImageRect(bitmap, viewSize)

    // Clamp crop rect to image bounds
    val intersected = RectF().apply {
        set(cropRect)
        intersect(imageRect)
    }

    val scaleX = bitmap.width / imageRect.width()
    val scaleY = bitmap.height / imageRect.height()

    val x = ((intersected.left - imageRect.left) * scaleX).toInt()
    val y = ((intersected.top - imageRect.top) * scaleY).toInt()
    val w = (intersected.width() * scaleX).toInt()
    val h = (intersected.height() * scaleY).toInt()

    return Bitmap.createBitmap(bitmap, x, y, w, h)
}

fun calculateImageRect(
    bitmap: Bitmap,
    viewSize: Size
): RectF {
    val imageRatio = bitmap.width.toFloat() / bitmap.height
    val viewRatio = viewSize.width / viewSize.height

    return if (imageRatio > viewRatio) {
        // image fits width
        val displayedHeight = viewSize.width / imageRatio
        val top = (viewSize.height - displayedHeight) / 2f
        RectF(0f, top, viewSize.width, top + displayedHeight)
    } else {
        // image fits height
        val displayedWidth = viewSize.height * imageRatio
        val left = (viewSize.width - displayedWidth) / 2f
        RectF(left, 0f, left + displayedWidth, viewSize.height)
    }
}


