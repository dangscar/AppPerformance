package com.nlhd.appperformance.Domain.Repository

import android.content.Context
import android.net.Uri
import androidx.paging.PagingData
import com.nlhd.appperformance.Data.Model.Video.Profile
import com.nlhd.appperformance.Domain.Entity.Video.Comment
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.Domain.Entity.Video.Video
import com.nlhd.appperformance.Utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun getVideos(token: String): Flow<PagingData<Video>>
    fun searchVideos(query: String, timestamp: Long): Flow<PagingData<Video>>
    fun clearSearchFlow(query: String, timestamp: Long)
    fun getVideosExplore(): Flow<PagingData<Video>>
    fun getVideosFollowing(token: String): Flow<PagingData<Video>>
    suspend fun like(token: String, videoId: String): ResultWrapper<MessageResponse>
    fun getComments(videoId: String): Flow<PagingData<Comment>>
    suspend fun addComment(token: String, videoId: String, content: String): ResultWrapper<MessageResponse>
    suspend fun getFollowing(token: String, videoId: String): ResultWrapper<MessageResponse>
    suspend fun uploadVideo(token: String, video: Uri, image: Uri?, caption: String?, context: Context): ResultWrapper<MessageResponse>
    suspend fun getProfile(userId: Int): ResultWrapper<Profile>
    fun getVideosProfile(userId: String, timestamp: Long): Flow<PagingData<Video>>
    fun clearProfileFlow(userId: String, timestamp: Long)
    suspend fun follow(token: String, userId: String): ResultWrapper<MessageResponse>
}