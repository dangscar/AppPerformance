package com.nlhd.appperformance.Domain.Entity.Login

data class User(
    val address: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String? = null,
    val email: String,
    val emailVerifiedAt: String? = null,
    val id: Int,
    val name: String,
    val phone: String? = null,
    val role: String
)