package com.kappa.app.core.network.model

import com.kappa.app.domain.audio.AudioRoom
import com.kappa.app.domain.audio.SeatMode
import com.kappa.app.domain.economy.CoinBalance
import com.kappa.app.domain.user.Role
import com.kappa.app.domain.user.User

fun UserDto.toDomain(): User {
    val resolvedRole = runCatching { Role.valueOf(role) }.getOrDefault(Role.USER)
    return User(
        id = id,
        username = username,
        email = email,
        role = resolvedRole,
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

fun RoomDto.toDomain(): AudioRoom {
    val resolvedSeatMode = runCatching { SeatMode.valueOf(seatMode) }.getOrDefault(SeatMode.FREE)
    return AudioRoom(
        id = id,
        name = name,
        isActive = isActive,
        seatMode = resolvedSeatMode,
        participantCount = participantCount
    )
}
