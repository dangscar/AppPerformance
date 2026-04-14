package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Repository.VideoRepository

class AddComment(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(token: String, videoId: String, content: String) = repository.addComment(token, videoId, content)
}