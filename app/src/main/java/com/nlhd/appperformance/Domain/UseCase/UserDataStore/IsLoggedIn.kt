package com.nlhd.appperformance.Domain.UseCase.UserDataStore

import com.nlhd.appperformance.Domain.Repository.UserDataStoreRepository

class IsLoggedIn(
    private val repository: UserDataStoreRepository
) {
    suspend operator fun invoke(): Boolean = repository.isLoggedIn()
}