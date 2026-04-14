package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Repository.VideoRepository

class GetFollowing(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(token: String, videoId: String) = repository.getFollowing(token, videoId)
}