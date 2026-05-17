package com.nlhd.appperformance.Data.Model.Video

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val avatar_url: String? = null,
    val can_follow: Int,
    val email: String,
    val followers_count: Int,
    val followings_count: Int,
    val id: Int,
    val is_following: Int,
    val name: String,
    val received_favorites_count: Int,
    val received_likes_count: Int,
    val videos_count: Int
)