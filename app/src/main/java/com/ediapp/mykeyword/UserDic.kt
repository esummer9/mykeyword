package com.ediapp.mykeyword

import android.content.Context
import androidx.compose.runtime.remember
import java.io.File


val posDisplayMap =
    linkedMapOf(
        "NNG" to "일반명사",
        "NNP" to "고유명사",
//        "NNB" to "의존명사",
//        "NP" to "대명사",
//        "NR" to "수사",
        "NA" to "제외(불능)"
    )

data class UserDicItem(val keyword: String, val pos: String)

fun WriteUserDic(context: Context, item: UserDicItem) {
    val userDicFile = File(context.filesDir, "komoran/user.dict")
    userDicFile.parentFile?.mkdirs()

    val lines = if (userDicFile.exists()) {
        userDicFile.readLines()
    } else {
        emptyList()
    }

    val newLines = mutableListOf<String>()
    var found = false
    for (line in lines) {
        if (line.startsWith(item.keyword + "\t")) {
            newLines.add("${item.keyword}\t${item.pos}")
            found = true
        } else {
            newLines.add(line)
        }
    }

    if (!found) {
        newLines.add("${item.keyword}\t${item.pos}")
    }

    userDicFile.writeText(newLines.joinToString("\n"))
}
