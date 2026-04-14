package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Repository.VideoRepository

class GetComments(
    private val repository: VideoRepository
) {
    operator fun invoke(videoId: String) = repository.getComments(videoId)
}
