package com.nlhd.appperformance.Domain.UseCase.UserDataStore

data class UserDataStoreUseCase(
    val saveUser: SaveUser,
    val getUser: GetUser,
    val clearUser: ClearUser,
    val isLoggedIn: IsLoggedIn
)
