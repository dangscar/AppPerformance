package com.nlhd.appperformance.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.cachedIn
import com.nlhd.appperformance.Domain.Entity.DataStore.UserPreference
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.UserDataStoreUseCase
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.Utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoUseCase: VideoUseCase,
    private val userDataStoreUseCase: UserDataStoreUseCase,
): ViewModel() {

    fun videosFollowing(token: String) = videoUseCase.getVideosFollowing(token)
    val userState = userDataStoreUseCase.getUser().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        UserPreference())

    val videos = userState
        .map { it.token }
        .filter { it.isNotEmpty() }
        .flatMapLatest { token ->
            videoUseCase.getVideos(token)
        }
        .cachedIn(viewModelScope)

    private var _currentPosition: MutableLiveData<Int> = MutableLiveData(0)
    val currentPosition: LiveData<Int> = _currentPosition

    private var _likeState: MutableLiveData<ResultUI<MessageResponse>> = MutableLiveData(ResultUI.Idle)
    val likeState: LiveData<ResultUI<MessageResponse>> = _likeState

    private var _isFollowing: MutableLiveData<ResultUI<MessageResponse>> = MutableLiveData(ResultUI.Idle)
    val isFollowing: LiveData<ResultUI<MessageResponse>> = _isFollowing

    fun getFollowing(userId: String) = viewModelScope.launch {
        _isFollowing.value = ResultUI.Loading
        userState.collect { userPreference ->
            videoUseCase.getFollowing(userPreference.token, userId).let { result ->
                when (result) {
                    is ResultWrapper.Error -> {
                        _isFollowing.value = ResultUI.Error(result.exception.message.toString())
                    }
                    is ResultWrapper.Success<*> -> {
                        _isFollowing.value = ResultUI.Success(result.value as MessageResponse)
                    }
                }
            }
        }

    }

    fun updateStateFollowing() {
        _isFollowing.value = ResultUI.Idle
    }

    fun like(token: String, videoId: String) = viewModelScope.launch {
        _likeState.value = ResultUI.Loading
        videoUseCase.like(token, videoId).let { result->
            when (result) {
                is ResultWrapper.Error -> {
                    _likeState.value = ResultUI.Error(result.exception.message.toString())
                }
                is ResultWrapper.Success<*> -> {
                    _likeState.value = ResultUI.Success(result.value as MessageResponse)
                }
            }
        }
    }

    fun updateStateLike() {
        _likeState.value = ResultUI.Idle
    }

    fun setCurrentPosition(position: Int) {
        if (_currentPosition.value != position) {
            _currentPosition.value = position
        }
    }
}