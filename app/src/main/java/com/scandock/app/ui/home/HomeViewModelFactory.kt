package com.scandock.app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scandock.app.util.DatabaseProvider

class HomeViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    val db = DatabaseProvider.get(context)

    private val repository = HomeRepository(
        db.scanDao()
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
