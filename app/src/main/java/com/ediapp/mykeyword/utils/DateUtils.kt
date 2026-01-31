package com.ediapp.mykeyword.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatRegDate(regDate: Long?): String {
    if (regDate == null) return ""

    val currentTime = System.currentTimeMillis()
    val diff = currentTime - regDate
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(regDate))
        hours > 0 -> "${hours}시간 전"
        minutes > 0 -> "${minutes}분 전"
        else -> "방금 전"
    }
}
