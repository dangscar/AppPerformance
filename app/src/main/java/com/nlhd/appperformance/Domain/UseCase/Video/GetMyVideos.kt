package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Repository.VideoRepository

class GetMyVideos(
    private val repository: VideoRepository
) {
    operator fun invoke(token: String) = repository.getMyVideos(token)
}