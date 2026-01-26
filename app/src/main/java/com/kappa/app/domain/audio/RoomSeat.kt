package com.kappa.app.domain.audio

data class RoomSeat(
    val seatNumber: Int,
    val status: SeatStatus,
    val userId: String? = null,
    val username: String? = null
)

enum class SeatStatus {
    FREE,
    OCCUPIED,
    BLOCKED
}
