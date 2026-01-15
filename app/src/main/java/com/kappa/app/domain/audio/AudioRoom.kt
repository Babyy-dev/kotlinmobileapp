package com.kappa.app.domain.audio

/**
 * AudioRoom domain model.
 */
data class AudioRoom(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val seatMode: SeatMode,
    val participantCount: Int
)

enum class SeatMode {
    FREE,
    BLOCKED
}
