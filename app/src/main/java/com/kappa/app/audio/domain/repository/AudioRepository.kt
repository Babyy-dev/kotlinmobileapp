package com.kappa.app.audio.domain.repository

import com.kappa.app.domain.audio.AudioRoom
import com.kappa.app.domain.audio.GiftLog
import com.kappa.app.domain.audio.RoomMessage

/**
 * Audio repository interface.
 */
interface AudioRepository {
    suspend fun getAudioRooms(): Result<List<AudioRoom>>
    suspend fun joinRoom(roomId: String): Result<JoinRoomInfo>
    suspend fun leaveRoom(roomId: String): Result<Unit>
    suspend fun getRoomMessages(roomId: String): Result<List<RoomMessage>>
    suspend fun sendRoomMessage(roomId: String, message: String): Result<RoomMessage>
    suspend fun getRoomGifts(roomId: String): Result<List<GiftLog>>
    suspend fun sendGift(roomId: String, amount: Long, recipientId: String?): Result<GiftLog>
}

data class JoinRoomInfo(
    val room: AudioRoom,
    val livekitUrl: String,
    val token: String
)
