package com.ediapp.mykeyword

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran
import kr.co.shineware.nlp.komoran.model.KomoranResult
import java.io.File

class KomoranManager(private val context: Context) {

    private val reloadMutex = Mutex()

    @Volatile
    private var komoran: Komoran = Komoran(DEFAULT_MODEL.FULL)

    // 필요 시 기분석 사전도 같이 쓰면 여기서 함께 세팅
    private var fwDicPath: String? = null

    private fun userDicFile(): File = File(context.filesDir, "komoran/user.dict")

    suspend fun analyze(text: String): KomoranResult {
        // komoran 참조는 volatile이라 스왑되어도 안전하게 최신/이전 중 하나를 사용
        return komoran.analyze(text)
    }

    /**
     * entries 예: listOf("동덕여대\tNNP", "배달앱\tNNG")
     */
    suspend fun updateUserDictionary(entries: List<String>) {
        reloadMutex.withLock {
            val dicFile = userDicFile()
            dicFile.parentFile?.mkdirs()

            // UTF-8 텍스트로 덮어쓰기 (원하면 기존 내용 읽어서 merge 가능)
            dicFile.writeText(entries.joinToString(separator = "\n", postfix = "\n"), Charsets.UTF_8)

            // 새 인스턴스 생성 + 사전 로드 후 스왑
            val newKomoran = Komoran(DEFAULT_MODEL.FULL)
            fwDicPath?.let { newKomoran.setFWDic(it) }
            newKomoran.setUserDic(dicFile.absolutePath) // 파일 경로로 로드 :contentReference[oaicite:2]{index=2}

            komoran = newKomoran
        }
    }

    /**
     * "한 단어씩" 추가하고 싶을 때: 기존 파일 읽어서 append -> 리로드
     */
    suspend fun addUserWord(word: String, pos: String? = null) {
        val line = if (pos.isNullOrBlank()) word else "$word\t$pos" // pos 생략 시 NNP로 인지 :contentReference[oaicite:3]{index=3}
        reloadMutex.withLock {
            val dicFile = userDicFile()
            dicFile.parentFile?.mkdirs()
            if (!dicFile.exists()) dicFile.writeText("", Charsets.UTF_8)

            // 중복 방지(선택): 이미 있으면 추가하지 않기
            val existing = dicFile.readLines(Charsets.UTF_8).toHashSet()
            if (!existing.contains(line)) {
                dicFile.appendText(line + "\n", Charsets.UTF_8)
            }

            val newKomoran = Komoran(DEFAULT_MODEL.FULL)
            fwDicPath?.let { newKomoran.setFWDic(it) }
            newKomoran.setUserDic(dicFile.absolutePath)
            komoran = newKomoran
        }
    }
}
