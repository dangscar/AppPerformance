package com.nlhd.appperformance.Data.Repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.nlhd.appperformance.DataStore.UserKeys
import com.nlhd.appperformance.DataStore.userDataStore
import com.nlhd.appperformance.Domain.Entity.DataStore.UserPreference
import com.nlhd.appperformance.Domain.Repository.UserDataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserDataStoreRepositoryImp(
    private val context: Context
): UserDataStoreRepository {

    //Lấy thông tin user
    override fun userFlow(): Flow<UserPreference> = context.userDataStore.data.map { prefs->
        UserPreference(
            name = prefs[UserKeys.NAME] ?: "",
            email = prefs[UserKeys.EMAIL] ?: "",
            avatarUrl = prefs[UserKeys.AVATAR_URL] ?: "",
            token = prefs[UserKeys.TOKEN] ?: "",
            isLoggedIn = prefs[UserKeys.IS_LOGGED_IN] ?: false,
            keyboardPadding = prefs[UserKeys.KEYBOARD_PADDING] ?: 0
        )
    }

    override suspend fun saveUser(userPreference: UserPreference) {
        context.userDataStore.edit { prefs->
            prefs[UserKeys.NAME] = userPreference.name
            prefs[UserKeys.EMAIL] = userPreference.email
            prefs[UserKeys.AVATAR_URL] = userPreference.avatarUrl
            prefs[UserKeys.TOKEN] = userPreference.token
            prefs[UserKeys.IS_LOGGED_IN] = userPreference.isLoggedIn
            prefs[UserKeys.KEYBOARD_PADDING] = userPreference.keyboardPadding
        }
    }

    override suspend fun clearUser() {
        context.userDataStore.edit { prefs->
            prefs.clear()
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        val prefs = context.userDataStore.data.first()
        return !prefs[UserKeys.TOKEN].isNullOrEmpty()
    }

    override suspend fun saveKeyboardPadding(padding: Int) {
        context.userDataStore.edit { prefs ->
            prefs[UserKeys.KEYBOARD_PADDING] = padding
        }
    }
}