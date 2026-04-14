package com.nlhd.appperformance.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlhd.appperformance.Domain.Entity.DataStore.UserPreference
import com.nlhd.appperformance.Domain.Entity.GetUser.User
import com.nlhd.appperformance.Domain.UseCase.Authenticate.AuthenticateUseCase
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.UserDataStoreUseCase
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.Utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDataStoreUseCase: UserDataStoreUseCase,
    private val authenticateUseCase: AuthenticateUseCase
): ViewModel() {
    val userState = userDataStoreUseCase.getUser().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        UserPreference())

    private var _state = MutableLiveData<ResultUI<User>>(ResultUI.Idle)
    val state get() = _state

    var currentOffset = 0

    private var _isCleared = MutableLiveData(false)
    val isCleared get() = _isCleared

    fun setCleared(cleared: Boolean) {
        _isCleared.value = cleared
    }

    fun getUser(token: String) = viewModelScope.launch {
        authenticateUseCase.getUser(token).let { result->
            when (result) {
                is ResultWrapper.Error -> {
                    _state.value = ResultUI.Error(result.exception.message.toString())
                }
                is ResultWrapper.Success<*> -> {
                    val user = result.value as User
                    _state.value = ResultUI.Success(user)
                    userDataStoreUseCase.saveUser(UserPreference(
                        name = user.name,
                        avatarUrl = user.avatarUrl ?: "",
                        email = user.email,
                        isLoggedIn = true,
                        token = token
                    ))
                }
            }
        }
    }


    fun logout() = viewModelScope.launch {
        userDataStoreUseCase.clearUser()
    }
}
