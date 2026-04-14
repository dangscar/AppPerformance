package com.nlhd.appperformance.Domain.UseCase.Authenticate

data class AuthenticateUseCase(
    val login: Login,
    val getUser: GetUser
)
