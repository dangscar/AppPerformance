package com.nlhd.appperformance.Data.Repository

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nlhd.appperformance.ApplicationScope
import com.nlhd.appperformance.Data.Mapper.toDomain
import com.nlhd.appperformance.Data.Model.Video.AddCommentRequestDto
import com.nlhd.appperformance.Data.Model.Video.MessageResponseDto
import com.nlhd.appperformance.Data.Model.Video.Profile
import com.nlhd.appperformance.Data.Remote.GetCommentsPagingSource
import com.nlhd.appperformance.Data.Remote.GetVideosPagingSource
import com.nlhd.appperformance.Data.Remote.SearchVideosPagingSource
import com.nlhd.appperformance.Data.Remote.VideosProfilePagingSource
import com.nlhd.appperformance.Domain.Entity.Video.Comment
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.Domain.Entity.Video.Video
import com.nlhd.appperformance.Domain.Repository.VideoRepository
import com.nlhd.appperformance.Utils.ResultWrapper
import com.nlhd.appperformance.Utils.TYPE_URL
import com.nlhd.appperformance.Utils.Utils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Singleton

@Singleton
class VideoRepositoryImp(
    private val ktor: HttpClient,
    @ApplicationScope private val appScope: CoroutineScope
): VideoRepository {
    private var feedFlow: Flow<PagingData<Video>>? = null
    private val searchFlows = mutableMapOf<String, Flow<PagingData<Video>>>()

    private var exploreFlow : Flow<PagingData<Video>>? = null

    private var followingFlow : Flow<PagingData<Video>>? = null
    private var profileFlow = mutableMapOf<String, Flow<PagingData<Video>>>()


    override fun getVideos(token: String): Flow<PagingData<Video>> {
        if (feedFlow == null) {
            feedFlow = Pager(
                config = PagingConfig(
                    pageSize = 3,
                    prefetchDistance = 1
                ),
                pagingSourceFactory = { GetVideosPagingSource(TYPE_URL.SUGGEST,ktor, token)},

                ).flow.cachedIn(appScope)
        }
        return feedFlow!!
    }

    override fun searchVideos(query: String, timestamp: Long): Flow<PagingData<Video>> {
        val queryKey = query.trim().lowercase()
        val uniqueKey = "${queryKey}_$timestamp"
        val searchVideos = searchFlows.getOrPut(uniqueKey) {
            Pager(
                config = PagingConfig(
                    pageSize = 3,
                    prefetchDistance = 1
                ),
                pagingSourceFactory = {
                    SearchVideosPagingSource(ktor, queryKey)
                }
            ).flow.cachedIn(appScope)
        }
        return searchVideos
    }

    override fun clearSearchFlow(query: String, timestamp: Long) {
        val uniqueKey = "${query.trim().lowercase()}_$timestamp"
        searchFlows.remove(uniqueKey)
    }

    override fun getVideosExplore(): Flow<PagingData<Video>> {
        if (exploreFlow == null) {
            exploreFlow= Pager(
                config = PagingConfig(
                    pageSize = 3,
                    prefetchDistance = 1
                ),
                pagingSourceFactory = { GetVideosPagingSource(TYPE_URL.EXPLORE,ktor )},

                ).flow.cachedIn(appScope)
        }
        return exploreFlow!!
    }

    override fun getVideosFollowing(token: String): Flow<PagingData<Video>> {
        if (followingFlow == null) {
            followingFlow = Pager(
                config = PagingConfig(
                    pageSize = 3,
                    prefetchDistance = 1
                ),
                pagingSourceFactory = { GetVideosPagingSource(TYPE_URL.FOLLOWING,ktor,token)},

                ).flow.cachedIn(appScope)
        }
        return followingFlow!!
    }

    override suspend fun like(
        token: String,
        videoId: String
    ): ResultWrapper<MessageResponse> {
        return try {
            val responseDto = ktor.post(Utils.BASE_URL+"/api/likes/$videoId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }.body<MessageResponseDto>()
            val response = responseDto.toDomain(responseDto)
            ResultWrapper.Success(response)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    override fun getComments(videoId: String): Flow<PagingData<Comment>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10
            ),
            pagingSourceFactory ={
                GetCommentsPagingSource(ktor, videoId)
            }
        ).flow
    }

    override suspend fun addComment(
        token: String,
        videoId: String,
        content: String
    ): ResultWrapper<MessageResponse> {
        return try {
            val responseDto = ktor.post(Utils.BASE_URL+"/api/comments") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(AddCommentRequestDto(
                    content = content,
                    video_id = videoId.toInt()
                ))
            }.body<MessageResponseDto>()
            val response = responseDto.toDomain(responseDto)
            ResultWrapper.Success(response)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    override suspend fun getFollowing(token: String, videoId: String): ResultWrapper<MessageResponse> {
        return try {
            val responseDto = ktor.get(Utils.BASE_URL+"/api/follows/${videoId}") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }.body<MessageResponseDto>()
            val response = responseDto.toDomain(responseDto)
            ResultWrapper.Success(response)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    private fun ContentResolver.displayName(uri: Uri): String {
        query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) return c.getString(0)
        }
        return uri.lastPathSegment ?: "upload"
    }
    private fun ContentResolver.mimeType(uri: Uri): String =
        getType(uri) ?: "application/octet-stream"
    private fun ContentResolver.length(uri: Uri): Long? =
        openAssetFileDescriptor(uri, "r")?.length

    fun getVideoThumbnail(context: Context, uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.getFrameAtTime(0) // lấy frame đầu tiên
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    fun bitmapToInputStream(bitmap: Bitmap): InputStream {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return ByteArrayInputStream(stream.toByteArray())
    }

    override suspend fun uploadVideo(
        token: String,
        video: Uri,
        image: Uri?,
        caption: String?,
        context: Context
    ): ResultWrapper<MessageResponse> {
        return try {
            val cr = context.contentResolver

            val multipart = MultiPartFormDataContent(
                formData {
                    // IMAGE (optional)
                    val thumbnail = getVideoThumbnail(context, video)

                    thumbnail?.let { bmp ->
                        val inputStream = bitmapToInputStream(bmp)

                        appendInput(
                            key = "image",
                            headers = Headers.build {
                                append(
                                    HttpHeaders.ContentType,
                                    "image/jpeg"
                                ) // ví dụ: image/jpeg
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "form-data; name=\"image\"; filename=\"${cr.displayName(video)}\""
                                )
                            },
                            size = null // có thể để null nếu không biết trước
                        ) { inputStream.asInput() }
                    }

                    // VIDEO (required)
                    val vid = video
                    appendInput(
                        key = "video",
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, cr.mimeType(vid)) // ví dụ: video/mp4
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"video\"; filename=\"${cr.displayName(vid)}\""
                            )
                        },
                        size = cr.length(vid)
                    ) { cr.openInputStream(vid)!!.asInput() }

                    // TEXT fields
                    append("caption", caption.orEmpty())
                }
            )

            val dto = ktor.post("${Utils.BASE_URL}/api/video") {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(multipart)
            }.body<MessageResponseDto>()
            ResultWrapper.Success(dto.toDomain(dto))
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    override suspend fun getProfile(userId: Int): ResultWrapper<Profile> {
        return try {
            val response = ktor.get(Utils.BASE_URL+"/api/video/profile/$userId") {
                contentType(ContentType.Application.Json)
            }.body<Profile>()
            ResultWrapper.Success(response)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }


    override fun getVideosProfile(userId: String, timestamp: Long, token: String): Flow<PagingData<Video>> {
        val uniqueKey = "${userId}_$timestamp"
        val profileVideos = profileFlow.getOrPut(uniqueKey) {
            Pager(
                config = PagingConfig(
                    pageSize = 3,
                    prefetchDistance = 1
                ),
                pagingSourceFactory = {
                    VideosProfilePagingSource(ktor, userId, token)
                }
            ).flow.cachedIn(appScope)
        }
        Log.d("AAA", "getVideosProfile: $profileFlow")
        return profileVideos
    }

    override fun clearProfileFlow(userId: String, timestamp: Long) {
        val uniqueKey = "${userId}_$timestamp"
        profileFlow.remove(uniqueKey)
        Log.d("AAA", "clearProfileFlow: $profileFlow")
    }

    override suspend fun follow(
        token: String,
        userId: String
    ): ResultWrapper<MessageResponse> {
        return try {
            val responseDto = ktor.post(Utils.BASE_URL+"/api/follows/${userId}") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }.body<MessageResponseDto>()
            val response = responseDto.toDomain(responseDto)
            ResultWrapper.Success(response)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

}