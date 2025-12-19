// MorphemeAnalyzer.kt

package com.ediapp.mykeyword

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran

class MorphemeAnalyzer {

    private var komoran: Komoran? = null
    // 초기화 완료 상태를 관리하기 위한 CompletableDeferred 객체
    private val initializationSignal = CompletableDeferred<Unit>()

    // 초기화 함수: 백그라운드에서 실행되고, 완료되면 신호를 보냄
    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (komoran == null) {
            komoran = Komoran(DEFAULT_MODEL.FULL)
            // 초기화가 완료되었음을 알림
            initializationSignal.complete(Unit)
        }
    }

    // 텍스트 분석 함수
    suspend fun analyzeText(input: String): List<String> {
        // analyzeText가 호출되면, 먼저 초기화가 완료될 때까지 기다림
        initializationSignal.await()

        return withContext(Dispatchers.Default) {
            // 이 시점에서는 komoran이 non-null임을 보장할 수 있음
            val result = komoran!!.analyze(input)
            result.tokenList.map { "${it.morph}/${it.pos}" }
        }
    }
}
