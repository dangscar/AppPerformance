package com.nlhd.appperformance.Feature.LoginScreen

import com.nlhd.appperformance.Data.Repository.AuthenticateRepositoryImp
import com.nlhd.appperformance.Domain.Repository.AuthenticateRepository
import com.nlhd.appperformance.Domain.UseCase.Authenticate.AuthenticateUseCase
import com.nlhd.appperformance.Domain.UseCase.Authenticate.GetUser
import com.nlhd.appperformance.Domain.UseCase.Authenticate.Login
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthenticateModule {

    @Singleton
    @Provides
    fun provideAuthenticateRepository(ktor: HttpClient): AuthenticateRepository = AuthenticateRepositoryImp(ktor)

    @Singleton
    @Provides
    fun provideAuthenticateUseCase(repository: AuthenticateRepository): AuthenticateUseCase = AuthenticateUseCase(
        login = Login(repository),
        getUser = GetUser(repository)
    )

}