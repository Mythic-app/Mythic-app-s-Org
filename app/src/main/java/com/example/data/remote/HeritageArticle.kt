package com.example.data.remote

data class HeritageArticle(
    val id: String,
    val siteName: String,
    val province: String,
    val description: String,
    val imageUrl: String, // Can be a local drawable name (e.g. "img_sigiriya") or a web URL
    val category: String,
    val unescoStatus: String = "UNESCO World Heritage Site",
    val era: String = "Ancient",
    val facts: String = "Historical facts",
    val initialLikes: Int
)
