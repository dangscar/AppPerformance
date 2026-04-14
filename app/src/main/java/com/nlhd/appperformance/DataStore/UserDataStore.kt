package com.nlhd.appperformance.DataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.userDataStore by preferencesDataStore(name = "user_datastore")

object UserKeys {
    val NAME = stringPreferencesKey("name")
    val EMAIL = stringPreferencesKey("email")
    val AVATAR_URL = stringPreferencesKey("avatar_url")
    val TOKEN = stringPreferencesKey("token")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
}