package com.nlhd.appperformance.Domain.UseCase.UserDataStore

import com.nlhd.appperformance.Domain.Repository.UserDataStoreRepository

class ClearUser(
    private val repository: UserDataStoreRepository
) {
    suspend operator fun invoke() = repository.clearUser()
}