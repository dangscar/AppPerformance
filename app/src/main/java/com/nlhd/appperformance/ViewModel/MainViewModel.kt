package com.nlhd.appperformance.ViewModel

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse
import com.nlhd.appperformance.Domain.UseCase.Padding.PaddingDataStoreUseCase
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.UserDataStoreUseCase
import com.nlhd.appperformance.Domain.UseCase.Video.VideoUseCase
import com.nlhd.appperformance.Utils.Follow
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.Utils.ResultWrapper
import com.nlhd.appperformance.Utils.TabSelected
import com.nlhd.appperformance.Utils.tabs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val videoUseCase: VideoUseCase,
    private val paddingDataStoreUseCase: PaddingDataStoreUseCase
): ViewModel() {
    private var _showBar = MutableLiveData(true)
    val showBar: LiveData<Boolean> = _showBar
    private var _isLandscape = MutableLiveData(false)
    val isLandscape: LiveData<Boolean> = _isLandscape

    //Màu sắc của bottomBar
    private var _colorBottomNav = MutableLiveData(Color.BLACK)
    val colorBottomNav: LiveData<Int> = _colorBottomNav

    //Giữ trạng thái tab hiện tại
    private var _currentTab = MutableLiveData(false)
    val currentTab: LiveData<Boolean> = _currentTab

    private var _tabSelected: MutableLiveData<TabSelected> = MutableLiveData(TabSelected.Suggestions)
    val tabSelected: LiveData<TabSelected> = _tabSelected

    private var _navigation: MutableLiveData<Navigation> = MutableLiveData(Navigation.Home)
    val navigation: LiveData<Navigation> = _navigation

    private var _idProfile = MutableLiveData<Int>(-1)
    val idProfile: LiveData<Int> = _idProfile

    private var _backPressed = MutableLiveData(false)
    val backPressed: LiveData<Boolean> = _backPressed

    private val _scrollToTop = MutableLiveData<Boolean>()
    val scrollToTop: LiveData<Boolean> = _scrollToTop

    private var _scrollToTopRecyclerView = MutableLiveData<Boolean>()
    val scrollToTopRecyclerView: LiveData<Boolean> = _scrollToTopRecyclerView

    fun setScrollToTopRecyclerView(scroll: Boolean) {
        _scrollToTopRecyclerView.value = scroll
    }

    fun triggerScrollToTop() {
        _scrollToTop.value = true
    }

    fun resetScrollToTop() {
        _scrollToTop.value = false
    }

    fun setBackPressed(back: Boolean) {
        _backPressed.value = back
    }

    fun setIdProfile(id: Int) {
        _idProfile.value = id
    }

    fun setNavigation(navigation: Navigation) {
        _navigation.value = navigation
    }

    //Thiết lập tab được chọn

    fun setTabSelected(tabSelected: TabSelected) {
        _tabSelected.value = tabSelected
    }

    //Set tab hiện tại
    fun setCurrentTab(set: Boolean) {
        _currentTab.value = set
    }

    //Thiết lập màu sắc của bottomBar
    fun setColorBottomNav(color: Int) {
        _colorBottomNav.value = color
    }

    fun setLandscape(isLandscape: Boolean) {
        _isLandscape.value = isLandscape
    }
    fun showBarAction(isShow: Boolean) {
        _showBar.value = isShow
    }

    //Refresh trang
    private var _refresh = MutableLiveData(false)
    val refresh: LiveData<Boolean> = _refresh

    fun setRefresh(refresh: Boolean) { _refresh.value = refresh }

    //BottomSheet
    private val _text = MutableLiveData<Int>(0)
    val text: LiveData<Int> = _text

    fun setText(value: Int) {
        _text.value = value
    }

    //Following
    private var _isFollowing = MutableLiveData<ResultUI<MessageResponse>>(ResultUI.Idle)
    val isFollowing: LiveData<ResultUI<MessageResponse>> = _isFollowing
    private var _followState = MutableLiveData<Follow>(Follow.NOT_FOLLOW)
    val followState = _followState
    fun setFollowState(state: Follow) {
        _followState.value = state
    }
    fun follow(token: String,userId: String) = viewModelScope.launch {
        _isFollowing.value = ResultUI.Loading
        videoUseCase.follow(token, userId).let { result->
            when (result) {
                is ResultWrapper.Error -> {
                    _isFollowing.value = ResultUI.Error(result.exception)
                    setFollowState(Follow.NOT_FOLLOW)
                }
                is ResultWrapper.Success<*> -> {
                    _isFollowing.value = ResultUI.Success(result.value as MessageResponse)
                    val message = (result.value as MessageResponse).message
                    if (message == "Follow thành công") {
                        setFollowState(Follow.FOLLOWED)
                    } else {
                        setFollowState(Follow.NOT_FOLLOW)
                    }
                }
            }
        }
    }
    fun setFollowIdle() {
        _isFollowing.value = ResultUI.Idle
    }

    private var _paddingKeyboard = MutableLiveData<Int>(0)
    val paddingKeyboard get() = _paddingKeyboard


    fun getPaddingKeyboard() = viewModelScope.launch {
        paddingDataStoreUseCase.getPadding().collect {
            if (it != 0) {
                _paddingKeyboard.value = it
            }

        }
    }

    fun savePaddingKeyboard(padding: Int) = viewModelScope.launch {
        if (padding != 0) {
            paddingDataStoreUseCase.savePadding(padding)
            _paddingKeyboard.value = padding
        }
    }

    init {
        getPaddingKeyboard()
    }
}