package com.nlhd.appperformance.Data.Model

import com.nlhd.appperformance.Data.Model.Video.User
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val can_follow: String,
    val caption: String,
    val comments_count: String,
    val created_at: String? = null,
    val favorites_count: String,
    val hashtags: String? = null,
    val id: Int,
    val is_favorited: String,
    val is_following: String,
    val is_liked: String,
    val likes_count: String,
    val shares: String,
    val thumbnail_url: String? = null,
    val updated_at: String,
    val user: User,
    val user_id: String,
    val video_url: String,
    val views: String
)