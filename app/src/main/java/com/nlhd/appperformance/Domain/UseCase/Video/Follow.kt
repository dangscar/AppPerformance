package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Repository.VideoRepository

class Follow(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(token: String, userId: String) = repository.follow(token, userId)
}