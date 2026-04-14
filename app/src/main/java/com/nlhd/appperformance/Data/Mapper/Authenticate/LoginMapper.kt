package com.nlhd.appperformance.Data.Mapper.Authenticate

import com.nlhd.appperformance.Data.Model.Login.LoginRequestDto
import com.nlhd.appperformance.Data.Model.Login.LoginResponseDto
import com.nlhd.appperformance.Data.Model.Login.User
import com.nlhd.appperformance.Domain.Entity.Login.LoginRequest
import com.nlhd.appperformance.Domain.Entity.Login.LoginResponse

fun LoginRequestDto.toDomain(loginRequestDto: LoginRequestDto): LoginRequest {
    return LoginRequest(
        email = loginRequestDto.email,
        password = loginRequestDto.password
    )
}

fun LoginResponseDto.toDomain(loginResponseDto: LoginResponseDto): LoginResponse {
    return LoginResponse(
        token = loginResponseDto.token,
        user = loginResponseDto.user.toDomain(loginResponseDto.user)
    )
}

fun User.toDomain(user: User): com.nlhd.appperformance.Domain.Entity.Login.User {
    return com.nlhd.appperformance.Domain.Entity.Login.User(
        address = user.address,
        avatarUrl = user.avatar_url,
        createdAt = user.created_at,
        email = user.email,
        emailVerifiedAt = user.email_verified_at,
        id = user.id,
        name = user.name,
        phone = user.phone,
        role = user.role
    )
}

