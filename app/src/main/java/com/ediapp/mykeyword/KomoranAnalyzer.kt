package com.ediapp.mykeyword

import android.content.Context
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran
import java.io.File

class KomoranAnalyzer(private val context: Context) {

    private var komoran: Komoran? = null
    private val initializationSignal = CompletableDeferred<Unit>()

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (komoran == null) {
            komoran = Komoran(DEFAULT_MODEL.FULL)
            reloadUserDic()
            initializationSignal.complete(Unit)
        }
    }

    suspend fun reloadUserDic() = withContext(Dispatchers.IO) {
        val userDicFile = File(context.filesDir, "komoran/user.dict")
        if (userDicFile.exists()) {
            komoran?.setUserDic(userDicFile.absolutePath)
        }
    }

    suspend fun analyzeText(input: String): List<String> {
        initializationSignal.await()

        return withContext(Dispatchers.Default) {
            val result = komoran!!.analyze(input)
            result.tokenList.map { "${it.morph}/${it.pos}" }
        }
    }
}
