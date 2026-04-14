package com.nlhd.appperformance.Data.Model.Video

import kotlinx.serialization.Serializable

@Serializable
data class GetCommentResponseDto(
    val `data`: List<Data>
)