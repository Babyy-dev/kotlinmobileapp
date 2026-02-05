package com.kappa.app.core.network.model

data class RoomDto(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val seatMode: String,
    val participantCount: Int,
    val maxSeats: Int = 28,
    val requiresPassword: Boolean = false,
    val country: String? = null,
    val region: String? = null,
    val agencyName: String? = null,
    val agencyIconUrl: String? = null,
    val roomCode: String? = null,
    val isFavorite: Boolean = false
)

data class JoinRoomDto(
    val room: RoomDto,
    val livekitUrl: String,
    val token: String
)
