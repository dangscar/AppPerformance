package com.nlhd.appperformance.Domain.UseCase.Video

import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.Domain.Repository.VideoRepository
import com.nlhd.appperformance.Utils.ResultWrapper

class Like(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(token: String, videoId: String): ResultWrapper<MessageResponse> {
        return repository.like(token, videoId)
    }
}