package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Repository.VideoRepository

class ClearSearchFlow(
    private val repository: VideoRepository
) {
    operator fun invoke(query: String, timestamp: Long) = repository.clearSearchFlow(query, timestamp)
}
