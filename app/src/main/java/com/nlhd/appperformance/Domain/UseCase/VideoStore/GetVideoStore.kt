package com.nlhd.appperformance.Domain.UseCase.VideoStore

import androidx.paging.PagingData
import com.nlhd.appperformance.Domain.Entity.VideoStore
import com.nlhd.appperformance.Domain.Repository.VideoStoreRepository
import kotlinx.coroutines.flow.Flow

class GetVideoStore(
    private val repository: VideoStoreRepository
) {
    operator fun invoke(): Flow<PagingData<VideoStore>> {
        return repository.getVideoStore()
    }
}