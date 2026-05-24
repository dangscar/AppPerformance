package com.nlhd.appperformance.Domain.Repository

import kotlinx.coroutines.flow.Flow

interface PaddingDataStoreRepository {
    suspend fun savePadding(padding: Int)
    val getPadding: Flow<Int>
}