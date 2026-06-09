package com.nlhd.appperformance.Domain.UseCase.Video

data class VideoUseCase(
    val getVideos: GetVideos,
    val searchVideos: SearchVideos,
    val clearSearchFlow: ClearSearchFlow,
    val getVideosExplore: GetVideosExplore,
    val getVideosFollowing: GetVideosFollowing,
    val like: Like,
    val getComments: GetComments,
    val addComment: AddComment,
    val getFollowing: GetFollowing,
    val uploadVideo: UploadVideo,
    val getProfile: GetProfile,
    val getVideoProfile: GetVideoProfile,
    val clearProfileFlow: ClearProfileFlow,
    val follow: Follow,
    val getMyVideos: GetMyVideos
)
