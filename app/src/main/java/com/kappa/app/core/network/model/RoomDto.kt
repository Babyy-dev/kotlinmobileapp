package com.kappa.app.core.network.model

data class RoomDto(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val seatMode: String,
    val participantCount: Int
)

data class JoinRoomDto(
    val room: RoomDto,
    val livekitUrl: String,
    val token: String
)
