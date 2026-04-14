package com.nlhd.appperformance.Data.Model.Video

import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val content: String,
    val created_at: String? = null,
    val id: Int,
    val updated_at: String? = null,
    val user: User,
    val user_id: Int,
    val video_id: Int
)