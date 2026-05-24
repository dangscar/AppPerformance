package com.nlhd.appperformance.ViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlhd.appperformance.Domain.UseCase.Padding.PaddingDataStoreUseCase
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.UserDataStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralViewModel @Inject constructor(
    private val useCase: PaddingDataStoreUseCase
): ViewModel() {

}