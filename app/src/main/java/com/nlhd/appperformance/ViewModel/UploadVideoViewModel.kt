package com.nlhd.appperformance.ViewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlhd.appperformance.Domain.Entity.DataStore.UserPreference
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.UserDataStoreUseCase
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.Utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    private val videoUseCase: VideoUseCase,
    private val userDataStoreUseCase: UserDataStoreUseCase
): ViewModel() {

    val userState = userDataStoreUseCase.getUser().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        UserPreference())
    private var _state = MutableLiveData<ResultUI<MessageResponse>>(ResultUI.Idle)
    val state get() = _state

    fun uploadVideo(token: String, video: Uri, image: Uri?, caption: String?, context: Context) = viewModelScope.launch {
        _state.value = ResultUI.Loading
        videoUseCase.uploadVideo(token, video, image, caption, context).let { result->
            when (result) {
                is ResultWrapper.Error -> {
                    _state.value = ResultUI.Error(result.exception.message.toString())
                }
                is ResultWrapper.Success<*> -> {
                    _state.value = ResultUI.Success(result.value as MessageResponse)
                }
            }
        }
    }
}