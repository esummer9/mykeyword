package com.ediapp.mykeyword


import android.app.Application
//import com.bab2min.kiwi.Kiwi
//import com.bab2min.kiwi.Kiwi.Companion.M_L

import kr.pe.bab2min.Kiwi
import kr.pe.bab2min.KiwiBuilder
//import kr.pe.bab2min.Kiwi


// 모델 로딩은 시간이 걸릴 수 있으므로, 비동기적으로 호출하는 것이 좋습니다.
// 예: CoroutineScope(Dispatchers.IO).launch { kiwi = Kiwi(model = M_L) }

class MyApplication : Application() {
    private val kiwi = Kiwi(2) // M_L(대형), M_M(중형), M_S(소형) 모델 중 선택 가능
    override fun onCreate() {
        super.onCreate()
        // 앱이 시작될 때 Firebase를 초기화합니다.
// Kiwi 객체는 무거운 리소스이므로, 싱글톤으로 한 번만 생성하여 앱 전체에서 재사용하는 것이 좋습니다.

    }
}
