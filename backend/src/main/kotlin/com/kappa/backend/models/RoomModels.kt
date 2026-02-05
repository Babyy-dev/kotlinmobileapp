package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
enum class SeatMode {
    FREE,
    BLOCKED
}

@Serializable
enum class SeatStatus {
    FREE,
    OCCUPIED,
    BLOCKED
}

@Serializable
enum class ParticipantRole {
    LISTENER,
    SPEAKER
}

@Serializable
data class RoomResponse(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val seatMode: SeatMode,
    val participantCount: Int,
    val maxSeats: Int,
    val requiresPassword: Boolean,
    val country: String? = null
)

@Serializable
data class JoinRoomResponse(
    val room: RoomResponse,
    val livekitUrl: String,
    val token: String
)

@Serializable
data class RoomSeatResponse(
    val seatNumber: Int,
    val status: SeatStatus,
    val userId: String? = null,
    val username: String? = null
)

@Serializable
data class RoomParticipantResponse(
    val userId: String,
    val username: String,
    val role: ParticipantRole,
    val seatNumber: Int? = null,
    val isMuted: Boolean = false
)

@Serializable
data class RoomCreateRequest(
    val name: String,
    val seatMode: SeatMode = SeatMode.FREE,
    val maxSeats: Int = 28,
    val password: String? = null,
    val country: String? = null
)

@Serializable
data class JoinRoomRequest(
    val password: String? = null
)

@Serializable
data class MuteParticipantRequest(
    val muted: Boolean
)
