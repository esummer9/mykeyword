package com.ediapp.mykeyword

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeMorphemeAnalyzer()
    }

    val morphemeAnalyzer = MorphemeAnalyzer()
    private fun initializeMorphemeAnalyzer() {
        // 앱의 생명주기와 연결된 CoroutineScope에서 백그라운드 초기화 실행
        // 이 작업은 앱 시작을 지연시키지 않음
        CoroutineScope(Dispatchers.Main).launch {
            morphemeAnalyzer.initialize()
        }
    }
    companion object {
    }
}
