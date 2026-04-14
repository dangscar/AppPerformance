package com.nlhd.appperformance.Feature.LoginScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlhd.appperformance.Domain.Entity.DataStore.UserPreference
import com.nlhd.appperformance.Domain.Entity.Login.LoginResponse
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
class LoginViewModel @Inject constructor(
    private val userDataStoreUseCase: UserDataStoreUseCase,
    private val authenticateUseCase: AuthenticateUseCase
): ViewModel() {
    val userState = userDataStoreUseCase.getUser().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        UserPreference())

    private val _state = MutableLiveData<ResultUI<LoginResponse>>(ResultUI.Idle)
    val state: LiveData<ResultUI<LoginResponse>> = _state

    fun saveUser(userPreference: UserPreference) = viewModelScope.launch {
        userDataStoreUseCase.saveUser(userPreference)
    }

    fun isLoggedIn() = viewModelScope.launch {
        userDataStoreUseCase.isLoggedIn.invoke()
    }

    fun logout() = viewModelScope.launch {
        userDataStoreUseCase.clearUser()
    }

    fun login(email: String, password: String, onDismissBottomSheet: () -> Unit) = viewModelScope.launch {
        _state.value = ResultUI.Loading
        authenticateUseCase.login(email, password).let { result->
            when (result) {
                is ResultWrapper.Success<*> -> {
                    val data = result.value as LoginResponse
                    _state.value = ResultUI.Success(data)
                    saveUser(UserPreference(
                        token = data.token,
                        name = data.user.name,
                        avatarUrl = data.user.avatarUrl ?: "",
                        email = data.user.email,
                        isLoggedIn = true
                    ))
                    onDismissBottomSheet()
                }
                is ResultWrapper.Error -> {
                    _state.value = ResultUI.Error(result.exception.message.toString())
                }
            }
        }
    }
}

sealed class LoginState {
    object Idle: LoginState()
    object Loading: LoginState()
    data class Error(val message: String): LoginState()
    data class Success(val data: LoginResponse): LoginState()
}