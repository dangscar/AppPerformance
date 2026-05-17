package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Repository.VideoRepository

class ClearProfileFlow(
    private val repository: VideoRepository
) {
    operator fun invoke(userId: String, timestamp: Long) = repository.clearProfileFlow(userId, timestamp)
}