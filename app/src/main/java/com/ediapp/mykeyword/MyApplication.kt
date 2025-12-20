package com.ediapp.mykeyword

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {



    // lateinit으로 초기화를 지연시킵니다.
    lateinit var morphemeAnalyzer: KomoranAnalyzer
       private set

    override fun onCreate() {
        super.onCreate()

        // Context가 완전히 준비된 onCreate에서 초기화합니다.
        morphemeAnalyzer = KomoranAnalyzer(this)

        // 코루틴을 사용하여 백그라운드에서 초기화를 수행합니다.
        CoroutineScope(Dispatchers.IO).launch {
            morphemeAnalyzer.initialize()
        }
    }
}