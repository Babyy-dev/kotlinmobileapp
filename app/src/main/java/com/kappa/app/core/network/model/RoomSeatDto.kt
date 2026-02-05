package com.kappa.app.core.network.model

data class RoomSeatDto(
    val seatNumber: Int,
    val status: String,
    val userId: String? = null,
    val username: String? = null
)

data class RoomCreateRequest(
    val name: String,
    val seatMode: String = "FREE",
    val maxSeats: Int = 28,
    val password: String? = null,
    val country: String? = null
)

data class JoinRoomRequest(
    val password: String? = null
)

data class MuteParticipantRequest(
    val muted: Boolean
)
