package com.kappa.app.audio.domain.repository

import com.kappa.app.domain.audio.AudioRoom
import com.kappa.app.domain.audio.GiftLog
import com.kappa.app.domain.audio.RoomMessage
import com.kappa.app.domain.audio.RoomSeat

/**
 * Audio repository interface.
 */
interface AudioRepository {
    suspend fun getAudioRooms(): Result<List<AudioRoom>>
    suspend fun getHomeBanners(): Result<List<com.kappa.app.domain.home.HomeBanner>>
    suspend fun getPopularMiniGames(): Result<List<com.kappa.app.domain.home.MiniGame>>
    suspend fun createRoom(name: String, password: String? = null): Result<AudioRoom>
    suspend fun joinRoom(roomId: String, password: String? = null): Result<JoinRoomInfo>
    suspend fun leaveRoom(roomId: String): Result<Unit>
    suspend fun closeRoom(roomId: String): Result<Unit>
    suspend fun getRoomSeats(roomId: String): Result<List<RoomSeat>>
    suspend fun takeSeat(roomId: String, seat: Int): Result<Unit>
    suspend fun leaveSeat(roomId: String, seat: Int): Result<Unit>
    suspend fun lockSeat(roomId: String, seat: Int): Result<Unit>
    suspend fun unlockSeat(roomId: String, seat: Int): Result<Unit>
    suspend fun requestSeat(roomId: String, seat: Int): Result<Unit>
    suspend fun muteParticipant(roomId: String, userId: String, muted: Boolean): Result<Unit>
    suspend fun kickParticipant(roomId: String, userId: String): Result<Unit>
    suspend fun banParticipant(roomId: String, userId: String): Result<Unit>
    suspend fun toggleRoomFavorite(roomId: String, favorite: Boolean): Result<AudioRoom>
    suspend fun getRoomMessages(roomId: String): Result<List<RoomMessage>>
    suspend fun sendRoomMessage(roomId: String, message: String): Result<RoomMessage>
    suspend fun getRoomGifts(roomId: String): Result<List<GiftLog>>
    suspend fun sendGift(
        roomId: String,
        amount: Long,
        recipientId: String?,
        giftId: String?,
        giftType: String?,
        target: String? = null,
        recipientIds: List<String>? = null
    ): Result<GiftLog>
    suspend fun getGiftCatalog(): Result<List<com.kappa.app.domain.audio.GiftCatalogItem>>
    suspend fun search(query: String): Result<com.kappa.app.domain.home.SearchResult>
}

data class JoinRoomInfo(
    val room: AudioRoom,
    val livekitUrl: String,
    val token: String
)
