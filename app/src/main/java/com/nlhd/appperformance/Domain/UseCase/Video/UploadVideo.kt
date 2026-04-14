package com.nlhd.appperformance.Domain.UseCase.Video

import android.content.Context
import android.net.Uri
import com.nlhd.appperformance.Domain.Repository.VideoRepository

class UploadVideo(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(token: String, video: Uri, image: Uri?, caption: String?, context: Context) = repository.uploadVideo(token, video, image, caption, context)
}
