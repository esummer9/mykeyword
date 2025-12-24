package com.ediapp.mykeyword

import android.content.Context
import java.io.File

data class UserDicItem(val keyword: String, val pos: String)

fun addOrUpdateUserDic(context: Context, item: UserDicItem) {
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
        if (line.startsWith(item.keyword + "	")) {
            newLines.add("${item.keyword}	${item.pos}")
            found = true
        } else {
            newLines.add(line)
        }
    }

    if (!found) {
        newLines.add("${item.keyword}	${item.pos}")
    }

    userDicFile.writeText(newLines.joinToString("\n"))
}
