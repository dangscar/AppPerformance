package com.nlhd.appperformance.Domain.Entity.Video

data class Reply(
    val id: Int,
    val commentId: Int,
    val content: String,
    val user: User,
    val createdAt: String? = null
)