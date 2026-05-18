package com.nlhd.appperformance.Domain.Repository

import com.nlhd.appperformance.Domain.Entity.DataStore.UserPreference
import kotlinx.coroutines.flow.Flow

interface UserDataStoreRepository {
    fun userFlow(): Flow<UserPreference>
    suspend fun saveUser(userPreference: UserPreference)
    suspend fun clearUser()
    suspend fun isLoggedIn(): Boolean
    suspend fun saveKeyboardPadding(padding: Int)
}