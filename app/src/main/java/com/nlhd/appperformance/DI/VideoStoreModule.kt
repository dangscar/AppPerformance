package com.nlhd.appperformance.DI

import android.content.Context
import com.nlhd.appperformance.ApplicationScope
import com.nlhd.appperformance.Data.Repository.VideoStoreRepositoryImp
import com.nlhd.appperformance.Domain.Repository.VideoStoreRepository
import com.nlhd.appperformance.Domain.UseCase.VideoStore.GetVideoStore
import com.nlhd.appperformance.Domain.UseCase.VideoStore.VideoStoreUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VideoStoreModule {
    @Provides
    @Singleton
    fun provideVideoRepository(@ApplicationContext context: Context, @ApplicationScope appScope: CoroutineScope): VideoStoreRepository {
        return VideoStoreRepositoryImp(context, appScope)
    }

    @Provides
    @Singleton
    fun provideVideoStoreUseCase(repository: VideoStoreRepository): VideoStoreUseCase {
        return VideoStoreUseCase(
            getVideoStore = GetVideoStore(repository)
        )
    }
}