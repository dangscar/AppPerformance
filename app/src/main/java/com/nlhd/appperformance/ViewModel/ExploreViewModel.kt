package com.nlhd.appperformance.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val videoUseCase: VideoUseCase
): ViewModel() {
    val videos = videoUseCase.getVideosExplore()
}