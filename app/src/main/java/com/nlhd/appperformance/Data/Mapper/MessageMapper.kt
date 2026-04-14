package com.nlhd.appperformance.Data.Mapper

import com.nlhd.appperformance.Data.Model.Video.MessageResponseDto
import com.nlhd.appperformance.Domain.Entity.Video.MessageResponse

fun MessageResponseDto.toDomain(messageResponseDto: MessageResponseDto): MessageResponse {
    return MessageResponse(
        message = messageResponseDto.message
    )
}