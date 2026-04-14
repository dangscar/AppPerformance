package com.nlhd.appperformance.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.cachedIn
import com.nlhd.appperformance.Domain.Entity.DataStore.UserPreference
import com.nlhd.appperformance.Domain.UseCase.VideoStore.VideoStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
@HiltViewModel
class VideoStoreViewModel @Inject constructor(
    private val videoStoreUseCase: VideoStoreUseCase,
    private val players: MutableMap<Int, ExoPlayer>
) : ViewModel() {

    val videoStore = videoStoreUseCase.getVideoStore()
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
}