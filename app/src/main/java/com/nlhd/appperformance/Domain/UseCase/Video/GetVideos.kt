package com.nlhd.appperformance.Domain.UseCase.Video

import androidx.paging.PagingData
import com.nlhd.appperformance.Domain.Entity.Video.Video
import com.nlhd.appperformance.Domain.Repository.VideoRepository
import kotlinx.coroutines.flow.Flow

class GetVideos(
    private val repository: VideoRepository
) {
    operator fun invoke(token: String): Flow<PagingData<Video>> = repository.getVideos(token)
}