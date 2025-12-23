package com.ediapp.mykeyword

import android.content.Context
import android.util.Log
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
            val userDicFile = copyUserDicFromAssets(context)
            komoran = Komoran(DEFAULT_MODEL.FULL)
            komoran?.setUserDic(userDicFile.absolutePath)
            initializationSignal.complete(Unit)
        }
    }

    private fun copyUserDicFromAssets(context: Context): File {
        Log.d("context.filesDir", context.filesDir.toString())
        val outFile = File(context.filesDir, "komoran/user.dict")
        outFile.parentFile?.mkdirs()

        if (!outFile.exists()) {
            context.assets.open("user.dict").use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return outFile
    }

    suspend fun analyzeText(input: String): List<String> {
        initializationSignal.await()

        return withContext(Dispatchers.Default) {
            val result = komoran!!.analyze(input)
            result.tokenList.map { "${it.morph}/${it.pos}" }
        }
    }
}
