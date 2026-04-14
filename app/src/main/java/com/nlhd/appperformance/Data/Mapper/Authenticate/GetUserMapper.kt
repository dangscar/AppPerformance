package com.nlhd.appperformance.Data.Mapper.Authenticate

import com.nlhd.appperformance.Data.Model.GetUser.UserDto
import com.nlhd.appperformance.Domain.Entity.GetUser.User

fun UserDto.toDomain(userDto: UserDto): User = User(
    address = userDto.address,
    avatarUrl = userDto.avatar_url,
    createdAt = userDto.created_at,
    email = userDto.email,
    followingsCount = userDto.followings_count,
    followersCount = userDto.followers_count,
    id = userDto.id,
    name = userDto.name,
    phone = userDto.phone,
    receivedLikesCount = userDto.received_likes_count,
    role = userDto.role
)