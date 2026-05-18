package com.nlhd.appperformance.Domain.Entity.DataStore

data class UserPreference(
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val token: String = "",
    val isLoggedIn: Boolean = true,
    val keyboardPadding: Int = 0
)