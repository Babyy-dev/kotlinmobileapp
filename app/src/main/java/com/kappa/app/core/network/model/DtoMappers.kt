package com.kappa.app.core.network.model

import com.kappa.app.auth.domain.model.OtpInfo
import com.kappa.app.domain.audio.AudioRoom
import com.kappa.app.domain.audio.GiftLog
import com.kappa.app.domain.audio.RoomMessage
import com.kappa.app.domain.audio.SeatMode
import com.kappa.app.domain.audio.RoomSeat
import com.kappa.app.domain.audio.SeatStatus
import com.kappa.app.domain.economy.CoinBalance
import com.kappa.app.domain.user.Role
import com.kappa.app.domain.user.User

fun UserDto.toDomain(): User {
    val resolvedRole = Role.fromApi(role)
    return User(
        id = id,
        username = username,
        email = email,
        role = resolvedRole,
        phone = phone,
        nickname = nickname,
        avatarUrl = avatarUrl,
        country = country,
        language = language,
        isGuest = isGuest,
        permissions = emptyList()
    )
}

fun CoinBalanceDto.toDomain(): CoinBalance {
    return CoinBalance(
        userId = userId,
        balance = balance,
        currency = currency
    )
}

fun PhoneOtpResponse.toDomain(): OtpInfo {
    return OtpInfo(
        phone = phone,
        code = code,
        expiresAt = expiresAt
    )
}

fun RoomDto.toDomain(): AudioRoom {
    val resolvedSeatMode = runCatching { SeatMode.valueOf(seatMode) }.getOrDefault(SeatMode.FREE)
    return AudioRoom(
        id = id,
        name = name,
        isActive = isActive,
        seatMode = resolvedSeatMode,
        participantCount = participantCount,
        maxSeats = maxSeats,
        requiresPassword = requiresPassword
    )
}

fun RoomSeatDto.toDomain(): RoomSeat {
    val resolvedStatus = runCatching { SeatStatus.valueOf(status) }.getOrDefault(SeatStatus.FREE)
    return RoomSeat(
        seatNumber = seatNumber,
        status = resolvedStatus,
        userId = userId,
        username = username
    )
}

fun RoomMessageDto.toDomain(): RoomMessage {
    return RoomMessage(
        id = id,
        userId = userId,
        username = username,
        message = message,
        createdAt = createdAt
    )
}

fun GiftSendDto.toDomain(): GiftLog {
    return GiftLog(
        id = id,
        senderId = senderId,
        recipientId = recipientId,
        amount = amount,
        senderBalance = senderBalance,
        createdAt = createdAt
    )
}

fun AgencyApplicationDto.toDomain(): com.kappa.app.agency.domain.model.AgencyApplication {
    return com.kappa.app.agency.domain.model.AgencyApplication(
        id = id,
        agencyName = agencyName,
        status = status,
        createdAt = createdAt,
        reviewedAt = reviewedAt
    )
}

fun ResellerApplicationDto.toDomain(): com.kappa.app.agency.domain.model.ResellerApplication {
    return com.kappa.app.agency.domain.model.ResellerApplication(
        id = id,
        status = status,
        createdAt = createdAt,
        reviewedAt = reviewedAt
    )
}

fun TeamDto.toDomain(): com.kappa.app.agency.domain.model.Team {
    return com.kappa.app.agency.domain.model.Team(
        id = id,
        name = name,
        ownerUserId = ownerUserId,
        agencyId = agencyId
    )
}

fun AgencyCommissionDto.toDomain(): com.kappa.app.agency.domain.model.AgencyCommission {
    return com.kappa.app.agency.domain.model.AgencyCommission(
        id = id,
        diamondsAmount = diamondsAmount,
        commissionUsd = commissionUsd,
        createdAt = createdAt
    )
}
