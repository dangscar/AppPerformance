package com.nlhd.appperformance.Domain.UseCase.Authenticate

import com.nlhd.appperformance.Domain.Repository.AuthenticateRepository

class GetUser(
    private val repository: AuthenticateRepository
) {
    suspend operator fun invoke(token: String) = repository.getUser(token)
}