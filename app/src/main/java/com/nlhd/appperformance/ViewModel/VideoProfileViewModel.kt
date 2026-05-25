package com.nlhd.appperformance.ViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.UserDataStoreUseCase
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class VideoProfileViewModel @Inject constructor(
    private val videoUseCase: VideoUseCase,
    private val userDataStoreUseCase: UserDataStoreUseCase
): ViewModel() {
    private var _timestamp = MutableLiveData(System.currentTimeMillis())
    val timestamp: MutableLiveData<Long> = _timestamp
    private var _token = MutableLiveData("")
    val token: MutableLiveData<String> = _token
    fun setToken(token: String) {
        _token.value = token
    }
    fun setTimestamp(timestamp: Long) {
        _timestamp.value = timestamp
    }
    fun getUser() = userDataStoreUseCase.getUser()

    fun videoProfile(userId: String, timestamp: Long, token: String) =
        videoUseCase.getVideoProfile(
            userId = userId,
            timestamp = timestamp,
            token = token
        )
    fun clearProfileFlow(userId: String, timestamp: Long) = videoUseCase.clearProfileFlow(userId, timestamp)
}