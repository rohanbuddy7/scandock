package com.scandock.app.ui.camera

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.scandock.app.ui.nav.ScanRoutes
import com.scandock.app.ui.scanner.DocScannerScreen
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewModelScope

@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigateToPreview.collect { uri ->
            navController.navigate(
                ScanRoutes.preview(
                    uri = Uri.encode(uri.toString()),
                    scanId = 0L,
                    from = ScanRoutes.CAMERA
                )
            )
        }
    }

    DocScannerScreen(
        onImageCaptured = { uri ->
            viewModel.onImageCaptured(uri)
        }
    )
}
