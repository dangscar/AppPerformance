package com.nlhd.appperformance.Domain.Entity.GetUser

data class User(
    val address: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String? = null,
    val email: String,
    val followersCount: Int,
    val followingsCount: Int,
    val id: Int,
    val name: String,
    val phone: String? = null,
    val receivedLikesCount: Int,
    val role: String,
    val updatedAt: String? = null
)