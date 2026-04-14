package com.nlhd.appperformance.Data.Model.GetUser

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val address: String? = null,
    val avatar_url: String? = null,
    val created_at: String? = null,
    val email: String,
    val email_verified_at: String? = null,
    val followers_count: Int,
    val followings_count: Int,
    val id: Int,
    val name: String,
    val phone: String? = null,
    val received_likes_count: Int,
    val role: String,
    val updated_at: String? = null
)