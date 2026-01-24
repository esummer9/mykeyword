package com.ediapp.mykeyword.ui.notey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ediapp.mykeyword.DatabaseHelper

class MemosViewModelFactory(private val dbHelper: DatabaseHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemosViewModel(dbHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
