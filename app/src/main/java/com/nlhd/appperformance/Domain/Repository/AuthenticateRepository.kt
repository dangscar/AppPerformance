package com.nlhd.appperformance.Domain.Repository

import com.nlhd.appperformance.Domain.Entity.GetUser.User
import com.nlhd.appperformance.Domain.Entity.Login.LoginResponse
import com.nlhd.appperformance.Utils.ResultWrapper

interface AuthenticateRepository {
    suspend fun login(email: String, password: String): ResultWrapper<LoginResponse>
    suspend fun getUser(token: String): ResultWrapper<User>
}