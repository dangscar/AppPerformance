package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Repository.VideoRepository

class GetVideosExplore(
    private val repository: VideoRepository
) {
    operator fun invoke() = repository.getVideosExplore()
}