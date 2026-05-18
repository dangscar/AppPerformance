package com.nlhd.appperformance.Domain.UseCase.UserDataStore

import com.nlhd.appperformance.Domain.Repository.UserDataStoreRepository

class SaveKeyboardPadding(
    private val repository: UserDataStoreRepository
) {
    suspend operator fun invoke(padding: Int) = repository.saveKeyboardPadding(padding)
}