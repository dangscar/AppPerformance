package com.nlhd.appperformance.Data.Model.Video

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val avatar_url: String? = null,
    val id: Int,
    val name: String
)