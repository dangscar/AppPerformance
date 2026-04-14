package com.nlhd.appperformance.Data.Model

import kotlinx.serialization.Serializable

@Serializable
data class VideoResponseDto(
    val `data`: List<Data>,
)