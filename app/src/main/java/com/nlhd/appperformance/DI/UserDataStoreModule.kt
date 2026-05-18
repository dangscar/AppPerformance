package com.nlhd.appperformance.DI

import android.content.Context
import com.nlhd.appperformance.Data.Repository.UserDataStoreRepositoryImp
import com.nlhd.appperformance.Domain.Repository.UserDataStoreRepository
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.ClearUser
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.GetUser
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.IsLoggedIn
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.SaveKeyboardPadding
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.SaveUser
import com.nlhd.appperformance.Domain.UseCase.UserDataStore.UserDataStoreUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserDataStoreModule {

    @Provides
    @Singleton
    fun provideUserDataStoreRepository(@ApplicationContext context: Context): UserDataStoreRepository {
        return UserDataStoreRepositoryImp(context)
    }

    @Provides
    @Singleton
    fun provideUserDataStoreUseCase(repository: UserDataStoreRepository): UserDataStoreUseCase {
        return UserDataStoreUseCase(
            saveUser = SaveUser(repository),
            getUser = GetUser(repository),
            clearUser = ClearUser(repository),
            isLoggedIn = IsLoggedIn(repository),
            saveKeyboardPadding = SaveKeyboardPadding(repository)
        )
    }
}