package com.nlhd.appperformance.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.UserDataStoreUseCase
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class SearchSuccessViewModel @Inject constructor(
    private val videoUseCase: VideoUseCase,
    private val userDataStoreUseCase: UserDataStoreUseCase
): ViewModel() {
    fun videos(query: String, timestamp: Long) = videoUseCase.searchVideos(query, timestamp)
    
    fun clearSearchFlow(query: String, timestamp: Long) {
        videoUseCase.clearSearchFlow(query, timestamp)
    }

    val videosExplore = videoUseCase.getVideosExplore()


    private var _currentPosition: MutableLiveData<Int> = MutableLiveData(0)
    val currentPosition: LiveData<Int> = _currentPosition

    private var _isCurrentItem: MutableLiveData<Boolean> = MutableLiveData(false)
    val isCurrentItem: LiveData<Boolean> = _isCurrentItem

    private var _infoVideo: MutableLiveData<String> = MutableLiveData("")
    val infoVideo: LiveData<String> = _infoVideo

    fun setInfoVideo(info: String) {
        _infoVideo.value = info
    }

    fun setCurrentItem(isCurrent: Boolean) {
        _isCurrentItem.value = isCurrent
    }

    fun setCurrentPosition(position: Int) {
        if (_currentPosition.value != position) {
            _currentPosition.value = position
        }
    }

    fun videosProfile(userId: String, timestamp: Long, token: String) = videoUseCase.getVideoProfile(
        userId = userId,
        timestamp = timestamp,
        token = token
    )

    fun clearVideosProfile(userId: String, timestamp: Long) = videoUseCase.clearProfileFlow(userId, timestamp)
}
