package com.nlhd.appperformance.Domain.UseCase.UserDataStore

import com.nlhd.appperformance.Domain.Entity.DataStore.UserPreference
import com.nlhd.appperformance.Domain.Repository.UserDataStoreRepository

class SaveUser(
    private val repository: UserDataStoreRepository
) {
    suspend operator fun invoke(userPreference: UserPreference) = repository.saveUser(userPreference)
}