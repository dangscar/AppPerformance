package com.nlhd.appperformance.Domain.Entity.Login

data class LoginResponse(
    val token: String,
    val user: User
)