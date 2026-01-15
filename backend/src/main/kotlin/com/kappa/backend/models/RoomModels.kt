package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
enum class SeatMode {
    FREE,
    BLOCKED
}

@Serializable
data class RoomResponse(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val seatMode: SeatMode,
    val participantCount: Int
)

@Serializable
data class JoinRoomResponse(
    val room: RoomResponse,
    val livekitUrl: String,
    val token: String
)
