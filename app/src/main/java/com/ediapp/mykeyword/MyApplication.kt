package com.ediapp.mykeyword

import android.app.Application
import android.content.Intent
import android.os.Build
import com.ediapp.mykeyword.service.NotificationService
import com.ediapp.mykeyword.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyApplication : Application() {

    lateinit var dbHelper: DatabaseHelper
        private set

    override fun onCreate() {
        super.onCreate()

        dbHelper = DatabaseHelper.getInstance(this)

        // Start NotificationService
        val serviceIntent = Intent(this, NotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    suspend fun updateKomoranUserDictionary() {
        withContext(Dispatchers.IO) {
            dbHelper.writeUserDictionaryToFile(applicationContext)
        }
    }
}