package com.nlhd.appperformance.Domain.UseCase.Padding

import com.nlhd.appperformance.Domain.Repository.PaddingDataStoreRepository

class SavePadding(
    private val repository: PaddingDataStoreRepository
) {
    suspend operator fun invoke(padding: Int) {
        repository.savePadding(padding)
    }
}