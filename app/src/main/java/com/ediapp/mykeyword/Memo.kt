package com.ediapp.mykeyword

data class Memo(
    val id: Long,
    val category: String?,
    val title: String?,
    val meaning: String?,
    val timestamp: Long,
    val regDate: Long?,
    val url: String?,
    val lat: Double?,
    val lon: Double?,
    val address: String?,
    val sido: String?,
    val sigungu: String?,
    val eupmyeondong: String?,
    val status: String?,
    val deleted_at: Int
)