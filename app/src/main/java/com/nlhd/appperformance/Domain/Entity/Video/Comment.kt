package com.nlhd.appperformance.Domain.Entity.Video

data class Comment(
    val content: String,
    val createdAt: String? = null,
    val id: Int,
    val user: User,
    val userId: Int,
    val videoId: Int,
    val replies: List<Reply>? = listOf(
        Reply(id = 1, commentId = 1,content = "Reply 1", user = User(id = 1, name = "User 1", avatarUrl = "")),
        Reply(id = 1, commentId = 1,content = "Hay qua, hay quá quá quá, \n jdd", user = User(id = 1, name = "User 1", avatarUrl = ""))
    ),
    var isExpanded: Boolean? = false
)