package com.nlhd.appperformance.Domain.UseCase.Authenticate

import com.nlhd.appperformance.Domain.Repository.AuthenticateRepository

class Login(
    private val repository: AuthenticateRepository
) {
    suspend operator fun invoke(email: String, password: String) = repository.login(email, password)
}