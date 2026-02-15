package com.scandock.app.ui.camera

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    private val _navigateToPreview = MutableSharedFlow<Uri>()
    val navigateToPreview = _navigateToPreview.asSharedFlow()

    fun onImageCaptured(uri: Uri) {
        viewModelScope.launch {
            _navigateToPreview.emit(uri)
        }
    }
}
