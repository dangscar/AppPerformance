package com.nlhd.appperformance.ViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.nlhd.appperformance.Domain.Entity.DataStore.UserPreference
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.UserDataStoreUseCase
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.Utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val videoUseCase: VideoUseCase,
    private val userDataStoreUseCase: UserDataStoreUseCase
): ViewModel() {

    private val videoIdFlow = MutableStateFlow<String?>(null)

    val userState = userDataStoreUseCase.getUser().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        UserPreference())
    private var _addCommentState = MutableLiveData<ResultUI<MessageResponse>>(ResultUI.Idle)
    val addCommentState get() = _addCommentState

    fun setAddCommentState(state: ResultUI<MessageResponse>) {
        _addCommentState.value = state
    }


    fun addComment(videoId: String, content: String) = viewModelScope.launch {
        _addCommentState.value = ResultUI.Loading
        userState.collect { userPreference ->
            if (!userPreference.isLoggedIn) {
                //Chua dang nhap
            } else {
                if (userPreference.token.isNotEmpty()) {
                    videoUseCase.addComment(userPreference.token, videoId, content).let { result->
                        when (result) {
                            is ResultWrapper.Error -> {
                                _addCommentState.value = ResultUI.Error(result.exception.message.toString())
                                Log.d("AAA", "Error in fun")
                            }
                            is ResultWrapper.Success<*> -> {
                                _addCommentState.value = ResultUI.Success(result.value as MessageResponse)
                                Log.d("AAA", "Success in fun")
                            }
                        }
                    }
                }
            }
        }

    }

    val commentFlow = videoIdFlow
        .filterNotNull()
        .flatMapLatest { id ->
            videoUseCase.getComments(id)
        }
        .cachedIn(viewModelScope)

    fun loadComment(videoId: String) {
        videoIdFlow.value = videoId
    }
}