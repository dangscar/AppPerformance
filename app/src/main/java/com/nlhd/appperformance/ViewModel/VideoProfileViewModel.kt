package com.nlhd.appperformance.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoProfileViewModel @Inject constructor(
    private val videoUseCase: VideoUseCase
): ViewModel() {
    private var _timestamp = MutableLiveData(System.currentTimeMillis())
    val timestamp: MutableLiveData<Long> = _timestamp
    fun setTimestamp(timestamp: Long) {
        _timestamp.value = timestamp
    }

    fun videoProfile(userId: String, timestamp: Long) = videoUseCase.getVideoProfile(userId, timestamp)
    fun clearProfileFlow(userId: String, timestamp: Long) = videoUseCase.clearProfileFlow(userId, timestamp)
}