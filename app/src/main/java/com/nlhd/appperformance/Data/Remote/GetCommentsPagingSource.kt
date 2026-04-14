package com.nlhd.appperformance.Data.Remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nlhd.appperformance.Data.Mapper.toDomain
import com.nlhd.appperformance.Data.Model.Video.GetCommentResponseDto
import com.nlhd.appperformance.Domain.Entity.Video.Comment
import com.nlhd.appperformance.Utils.Utils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType

class GetCommentsPagingSource(
    private val ktor: HttpClient,
    private val videoId: String
): PagingSource<Int, Comment>() {
    override fun getRefreshKey(state: PagingState<Int, Comment>): Int? {
        return state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Comment> {
        val page = params.key ?: 1
        return try {
            val responseDto = ktor.get(Utils.BASE_URL+"/api/comments/$videoId?page=$page") {
                contentType(ContentType.Application.Json)
            }.body<GetCommentResponseDto>()
            val response = responseDto.toDomain(responseDto)
            val endOfPageReached = response.data.isEmpty()
            if (!endOfPageReached) {
                LoadResult.Page(
                    data = response.data,
                    prevKey = if (page == 1) null else page + 1,
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
            LoadResult.Error(e)
        }
    }
}