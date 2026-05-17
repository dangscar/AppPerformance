package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Repository.VideoRepository

class GetProfile(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(userId: Int) = repository.getProfile(userId)
}