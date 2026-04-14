package com.nlhd.appperformance.Data.Model.Login

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val address: String? = null,
    val avatar_url: String? = null,
    val created_at: String? = null,
    val email: String,
    val email_verified_at: String? = null,
    val id: Int,
    val name: String,
    val phone: String? = null,
    val role: String
)