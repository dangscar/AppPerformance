package com.nlhd.appperformance.DI

import androidx.media3.exoplayer.ExoPlayer
import com.nlhd.appperformance.ApplicationScope
import com.nlhd.appperformance.Data.Repository.VideoRepositoryImp
import com.nlhd.appperformance.Domain.Repository.VideoRepository
import com.nlhd.appperformance.Domain.UseCase.Video.AddComment
import com.nlhd.appperformance.Domain.UseCase.Video.ClearSearchFlow
import com.nlhd.appperformance.Domain.UseCase.Video.GetComments
import com.nlhd.appperformance.Domain.UseCase.Video.GetFollowing
import com.nlhd.appperformance.Domain.UseCase.Video.GetVideos
import com.nlhd.appperformance.Domain.UseCase.Video.GetVideosExplore
import com.nlhd.appperformance.Domain.UseCase.Video.GetVideosFollowing
import com.nlhd.appperformance.Domain.UseCase.Video.Like
import com.nlhd.appperformance.Domain.UseCase.Video.SearchVideos
import com.nlhd.appperformance.Domain.UseCase.Video.UploadVideo
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
            clearSearchFlow = ClearSearchFlow(repository)
        )
    }

    @Provides
    @Singleton
    fun providePlayerMap(): MutableMap<Int, ExoPlayer> {
        return mutableMapOf()
    }
}