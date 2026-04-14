package com.nlhd.appperformance.Domain.Entity.Video

data class Comment(
    val content: String,
    val createdAt: String? = null,
    val id: Int,
    val user: User,
    val userId: Int,
    val videoId: Int
)