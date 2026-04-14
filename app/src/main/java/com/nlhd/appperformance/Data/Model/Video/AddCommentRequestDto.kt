package com.nlhd.appperformance.Data.Model.Video

import kotlinx.serialization.Serializable

@Serializable
data class AddCommentRequestDto(
    val content: String,
    val video_id: Int
)