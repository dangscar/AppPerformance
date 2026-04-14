package com.nlhd.appperformance.ViewModel

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.Utils.TabSelected
import com.nlhd.appperformance.Utils.tabs
import javax.inject.Inject

class MainViewModel @Inject constructor(

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
}