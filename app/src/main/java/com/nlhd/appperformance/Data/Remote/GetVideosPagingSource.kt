package com.nlhd.appperformance.Data.Remote

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nlhd.appperformance.Data.Mapper.Authenticate.toDomain
import com.nlhd.appperformance.Data.Mapper.toDomain
import com.nlhd.appperformance.Data.Model.VideoResponseDto
import com.nlhd.appperformance.Domain.Entity.Video.Video
import com.nlhd.appperformance.Utils.TYPE_URL
import com.nlhd.appperformance.Utils.Utils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType

class GetVideosPagingSource(
    private val typeUrl: TYPE_URL,
    private val ktor: HttpClient,
    private val token: String = ""
): PagingSource<Int, Video>() {
    fun url(typeUrl: TYPE_URL, page: Int): String {
        return when (typeUrl) {
            TYPE_URL.SUGGEST -> {
                Utils.BASE_URL+"/api/video?page=$page"
            }
            TYPE_URL.EXPLORE -> {
                Utils.BASE_URL+"/api/videoExplore?page=$page"
            }

            TYPE_URL.FOLLOWING -> {
                Utils.BASE_URL+"/api/video/videoFollow?page=$page"
            }

            TYPE_URL.MY_VIDEO -> {
                Utils.BASE_URL+"/api/video/myVideo?page=$page"
            }
        }
    }
    override fun getRefreshKey(state: PagingState<Int, Video>): Int? {
        return state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Video> {
        val page = params.key ?: 1
        return try {
            val responseDto = ktor.get(url(typeUrl, page)) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
            }.body<VideoResponseDto>()
            val response = responseDto.toDomain(responseDto)
            val endOfPageReached = response.videos.isEmpty()
            if (!endOfPageReached) {
                LoadResult.Page(
                    data = response.videos,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = page + 1
                )
            } else {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }
        } catch (e: Exception) {
            //Log.d("AAA", e.message.toString())
            LoadResult.Error(e)
        }
    }
}