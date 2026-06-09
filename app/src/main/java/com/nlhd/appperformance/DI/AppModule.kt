package com.nlhd.appperformance.DI

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.nlhd.appperformance.ApplicationScope
import com.nlhd.appperformance.Data.Repository.PaddingDataStoreImp
import com.nlhd.appperformance.Data.Repository.VideoRepositoryImp
import com.nlhd.appperformance.Domain.Repository.PaddingDataStoreRepository
import com.nlhd.appperformance.Domain.Repository.VideoRepository
import com.nlhd.appperformance.Domain.UseCase.Padding.GetPadding
import com.nlhd.appperformance.Domain.UseCase.Padding.PaddingDataStoreUseCase
import com.nlhd.appperformance.Domain.UseCase.Padding.SavePadding
import com.nlhd.appperformance.Domain.UseCase.Video.AddComment
import com.nlhd.appperformance.Domain.UseCase.Video.ClearProfileFlow
import com.nlhd.appperformance.Domain.UseCase.Video.ClearSearchFlow
import com.nlhd.appperformance.Domain.UseCase.Video.Follow
import com.nlhd.appperformance.Domain.UseCase.Video.GetComments
import com.nlhd.appperformance.Domain.UseCase.Video.GetFollowing
import com.nlhd.appperformance.Domain.UseCase.Video.GetMyVideos
import com.nlhd.appperformance.Domain.UseCase.Video.GetProfile
import com.nlhd.appperformance.Domain.UseCase.Video.GetVideoProfile
import com.nlhd.appperformance.Domain.UseCase.Video.GetVideos
import com.nlhd.appperformance.Domain.UseCase.Video.GetVideosExplore
import com.nlhd.appperformance.Domain.UseCase.Video.GetVideosFollowing
import com.nlhd.appperformance.Domain.UseCase.Video.Like
import com.nlhd.appperformance.Domain.UseCase.Video.SearchVideos
import com.nlhd.appperformance.Domain.UseCase.Video.UploadVideo
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import com.nlhd.appperformance.Utils.AudioEffects
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @ApplicationScope
    fun provideAppScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient {
        return HttpClient(CIO) {

            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }

            defaultRequest {
                header("Content-Type", "application/json")
            }
        }
    }

    @Provides
    @Singleton
    fun provideVideoRepository(ktor: HttpClient, @ApplicationScope appScope: CoroutineScope): VideoRepository {
        return VideoRepositoryImp(ktor, appScope)
    }

    @Provides
    @Singleton
    fun provideVideoUseCase(repository: VideoRepository): VideoUseCase {
        return VideoUseCase(
            getVideos = GetVideos(repository),
            searchVideos = SearchVideos(repository),
            getVideosExplore = GetVideosExplore(repository),
            getVideosFollowing = GetVideosFollowing(repository),
            like = Like(repository),
            getComments = GetComments(repository),
            addComment = AddComment(repository),
            getFollowing = GetFollowing(repository),
            uploadVideo = UploadVideo(repository),
            clearSearchFlow = ClearSearchFlow(repository),
            getProfile = GetProfile(repository),
            getVideoProfile = GetVideoProfile(repository),
            clearProfileFlow = ClearProfileFlow(repository),
            follow = Follow(repository),
            getMyVideos = GetMyVideos(repository)
        )
    }

    @Provides
    @Singleton
    fun providePaddingDataStore(@ApplicationContext context: Context): PaddingDataStoreRepository {
        return PaddingDataStoreImp(context)
    }

    @Provides
    @Singleton
    fun providePaddingDataStoreUseCase(repository: PaddingDataStoreRepository): PaddingDataStoreUseCase {
        return PaddingDataStoreUseCase(
            getPadding = GetPadding(repository),
            savePadding = SavePadding(repository)
        )
    }

    @Provides
    @Singleton
    fun providePlayerMap(): MutableMap<Int, ExoPlayer> {
        return mutableMapOf()
    }

    @Provides
    @Singleton
    fun provideEffectMap(): MutableMap<Int, AudioEffects> {
        return mutableMapOf()
    }
}