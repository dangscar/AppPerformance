package com.nlhd.appperformance.Data.Model.Test

data class Data(
    val can_follow: Int,
    val caption: String,
    val comments_count: Int,
    val created_at: String,
    val favorites_count: Int,
    val hashtags: String,
    val id: Int,
    val is_favorited: Int,
    val is_following: Int,
    val is_liked: Int,
    val likes_count: Int,
    val shares: Int,
    val thumbnail_url: String,
    val updated_at: String,
    val user: User,
    val user_id: Int,
    val video_url: String,
    val views: Int
)