package com.nlhd.appperformance.Domain.UseCase.UserDataStore

import com.nlhd.appperformance.Domain.Entity.DataStore.UserPreference
import com.nlhd.appperformance.Domain.Repository.UserDataStoreRepository
import kotlinx.coroutines.flow.Flow

class GetUser(
    private val repository: UserDataStoreRepository
) {
    operator fun invoke(): Flow<UserPreference> = repository.userFlow()
}