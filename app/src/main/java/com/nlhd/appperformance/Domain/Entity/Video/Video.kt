package com.nlhd.appperformance.Domain.Entity.Video

import com.nlhd.appperformance.Domain.Entity.Video.User

data class Video(
    val canFollow: String,
    val caption: String,
    var commentsCount: String,
    val createdAt: String? = null,
    val favoritesCount: String,
    val hashtags: String? = null,
    val id: Int,
    val isFavorite: String,
    val isFollowing: String,
    var isLiked: String,
    var likesCount: String,
    val shares: String,
    val thumbnailUrl: String? = null,
    val updatedAt: String,
    val user: User,
    val userId: String,
    val videoUrl: String,
    val views: String
)