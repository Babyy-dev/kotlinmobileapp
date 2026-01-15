package com.kappa.app.audio.domain.repository

import com.kappa.app.domain.audio.AudioRoom

/**
 * Audio repository interface.
 */
interface AudioRepository {
    suspend fun getAudioRooms(): Result<List<AudioRoom>>
    suspend fun joinRoom(roomId: String): Result<JoinRoomInfo>
    suspend fun leaveRoom(roomId: String): Result<Unit>
}

data class JoinRoomInfo(
    val room: AudioRoom,
    val livekitUrl: String,
    val token: String
)
