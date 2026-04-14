package com.nlhd.appperformance.Data.Mapper

import android.annotation.SuppressLint
import com.nlhd.appperformance.Data.Model.Data
import com.nlhd.appperformance.Data.Model.Video.User
import com.nlhd.appperformance.Data.Model.Video.GetCommentResponseDto
import com.nlhd.appperformance.Data.Model.VideoResponseDto
import com.nlhd.appperformance.Domain.Entity.Video.GetCommentResponse
import com.nlhd.appperformance.Domain.Entity.Video.Video
import com.nlhd.appperformance.Domain.Entity.Video.VideoResponse

fun VideoResponseDto.toDomain(videoResponseDto: VideoResponseDto): VideoResponse = VideoResponse(
    videos = videoResponseDto.data.map { it.toDomain(it) }
)

@SuppressLint("DefaultLocale")
fun Int.toShortString(): String {
    return when {
        this >= 1_000_000_000 -> String.format("%.1fB", this / 1_000_000_000.0).removeSuffix(".0")
        this >= 1_000_000     -> String.format("%.1fM", this / 1_000_000.0).removeSuffix(".0")
        this >= 100_000       -> String.format("%dK", this / 1_000) // 100K, 250K...
        this >= 10_000        -> String.format("%dK", this / 1_000) // 10K, 15K...
        this >= 1_000         -> String.format("%.1fK", this / 1_000.0).removeSuffix(".0")
        else                  -> this.toString()
    }
}

fun Data.toDomain(data: Data): Video = Video(
    canFollow = can_follow,
    caption = caption,
    commentsCount = comments_count.toInt().toShortString(),
    createdAt = created_at,
    favoritesCount = favorites_count.toInt().toShortString(),
    hashtags = hashtags,
    id = id,
    isFavorite = is_favorited,
    isFollowing = is_following,
    isLiked = is_liked,
    likesCount = likes_count.toInt().toShortString(),
    shares = shares.toInt().toShortString(),
    thumbnailUrl = thumbnail_url,
    updatedAt = updated_at,
    user = user.toDomain(user),
    userId = user_id,
    videoUrl = video_url,
    views = views
)

fun User.toDomain(user: User) = com.nlhd.appperformance.Domain.Entity.Video.User(
    avatarUrl = avatar_url,
    id = id,
    name = name
)

fun GetCommentResponseDto.toDomain(getCommentResponseDto: GetCommentResponseDto): GetCommentResponse {
    return GetCommentResponse(
        data = getCommentResponseDto.data.map { it.toDomain(it) }
    )
}

fun com.nlhd.appperformance.Data.Model.Video.Data.toDomain(data: com.nlhd.appperformance.Data.Model.Video.Data): com.nlhd.appperformance.Domain.Entity.Video.Comment {
    return com.nlhd.appperformance.Domain.Entity.Video.Comment(
        content = content,
        createdAt = created_at,
        id = id,
        user = user.toDomain(user),
        userId = user_id,
        videoId = video_id
    )
}
