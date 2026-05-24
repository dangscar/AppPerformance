package com.nlhd.appperformance.Data.Repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nlhd.appperformance.Domain.Repository.PaddingDataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension DataStore
private val Context.dataStore by preferencesDataStore(name = "app_settings")

class PaddingDataStoreImp(private val context: Context): PaddingDataStoreRepository {

    companion object {
        private val PADDING_KEY = intPreferencesKey("padding_key")
    }

    /**
     * Save padding
     */
    override suspend fun savePadding(padding: Int) {
        context.dataStore.edit { preferences ->
            preferences[PADDING_KEY] = padding
        }
    }

    /**
     * Get padding
     */
    override val getPadding: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PADDING_KEY] ?: 0
    }
}