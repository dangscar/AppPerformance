package com.nlhd.appperformance.Domain.Repository

import androidx.paging.PagingData
import com.nlhd.appperformance.Domain.Entity.VideoStore
import kotlinx.coroutines.flow.Flow

interface VideoStoreRepository {
    fun getVideoStore(): Flow<PagingData<VideoStore>>
}