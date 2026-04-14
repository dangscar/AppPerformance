package com.nlhd.appperformance.Data.Repository

import com.nlhd.appperformance.Data.Mapper.Authenticate.toDomain
import com.nlhd.appperformance.Data.Model.GetUser.UserDto
import com.nlhd.appperformance.Data.Model.Login.LoginRequestDto
import com.nlhd.appperformance.Data.Model.Login.LoginResponseDto
import com.nlhd.appperformance.Domain.Entity.GetUser.User
import com.nlhd.appperformance.Domain.Entity.Login.LoginResponse
import com.nlhd.appperformance.Domain.Repository.AuthenticateRepository
import com.nlhd.appperformance.Utils.ResultWrapper
import com.nlhd.appperformance.Utils.Utils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthenticateRepositoryImp(
    private val ktor: HttpClient
): AuthenticateRepository {
    override suspend fun login(
        email: String,
        password: String
    ): ResultWrapper<LoginResponse> {
        return try {
            val responseDto = ktor.post(Utils.BASE_URL+"/api/login") { //Post giá trị gì
                contentType(ContentType.Application.Json)
                setBody(
                    LoginRequestDto(
                        email = email,
                        password = password
                    )
                )
            }.body<LoginResponseDto>()
            val response = responseDto.toDomain(responseDto)
            ResultWrapper.Success(response)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }

    override suspend fun getUser(token: String): ResultWrapper<User> {
        return try {
            val responseDto = ktor.get(Utils.BASE_URL+"/api/user") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
            }.body<UserDto>()
            val response = responseDto.toDomain(responseDto)
            ResultWrapper.Success(response)
        } catch (e: Exception) {
            ResultWrapper.Error(e)
        }
    }
}
