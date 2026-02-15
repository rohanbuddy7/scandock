package com.scandock.app.ui.preview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scandock.app.util.DatabaseProvider

class PreviewViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    val db = DatabaseProvider.get(context)

    private val repository = PreviewRepository(
        db.scanDao(),
        db.pageDao()
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PreviewViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
