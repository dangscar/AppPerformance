package com.nlhd.appperformance.Domain.UseCase.Padding

import com.nlhd.appperformance.Domain.Repository.PaddingDataStoreRepository
import kotlinx.coroutines.flow.Flow

class GetPadding(
    private val repository: PaddingDataStoreRepository
) {
    operator fun invoke(): Flow<Int> {
        return repository.getPadding
    }
}