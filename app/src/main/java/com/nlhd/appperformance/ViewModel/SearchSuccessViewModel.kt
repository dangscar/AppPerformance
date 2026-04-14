package com.nlhd.appperformance.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class SearchSuccessViewModel @Inject constructor(
    private val videoUseCase: VideoUseCase
): ViewModel() {
    fun videos(query: String) = videoUseCase.searchVideos(query)
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

}