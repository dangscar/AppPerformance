package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Repository.VideoRepository

class SearchVideos(
    private val repository: VideoRepository
) {
    operator fun invoke(query: String, timestamp: Long) = repository.searchVideos(query, timestamp)
}