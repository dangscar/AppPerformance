package com.nlhd.appperformance.Data.Repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nlhd.appperformance.ApplicationScope
import com.nlhd.appperformance.Data.Remote.VideoStorePagingSource
import com.nlhd.appperformance.Domain.Entity.VideoStore
import com.nlhd.appperformance.Domain.Repository.VideoStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class VideoStoreRepositoryImp(
    private var context: Context,
    @ApplicationScope private val appScope: CoroutineScope
): VideoStoreRepository {
    private var feedFlow: Flow<PagingData<VideoStore>>? = null
    override fun getVideoStore(): Flow<PagingData<VideoStore>> {
        if (feedFlow == null) {
            feedFlow = Pager(
                config = PagingConfig(
                    pageSize = 3,
                    prefetchDistance = 1
                ),
                pagingSourceFactory = { VideoStorePagingSource(context) }
            ).flow.cachedIn(appScope)
        }
        return feedFlow!!
    }
}