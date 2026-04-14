package com.nlhd.appperformance.Data.Model.Login

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val token: String,
    val user: User
)