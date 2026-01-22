package com.kappa.app.audio.data.repository

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.audio.domain.repository.JoinRoomInfo
import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.ErrorMapper
import com.kappa.app.core.network.model.GiftSendRequest
import com.kappa.app.core.network.model.RoomMessageRequest
import com.kappa.app.core.network.model.toDomain
import com.kappa.app.domain.audio.GiftLog
import com.kappa.app.domain.audio.RoomMessage
import javax.inject.Inject

class RemoteAudioRepository @Inject constructor(
    private val apiService: ApiService,
    private val errorMapper: ErrorMapper
) : AudioRepository {
    override suspend fun getAudioRooms(): Result<List<com.kappa.app.domain.audio.AudioRoom>> {
        return try {
            val response = apiService.getRooms()
            val rooms = response.data
            if (!response.success || rooms == null) {
                Result.failure(Exception(response.error ?: "Failed to load rooms"))
            } else {
                Result.success(rooms.map { it.toDomain() })
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun joinRoom(roomId: String): Result<JoinRoomInfo> {
        return try {
            val response = apiService.joinRoom(roomId, null)
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "Failed to join room"))
            } else {
                Result.success(
                    JoinRoomInfo(
                        room = data.room.toDomain(),
                        livekitUrl = data.livekitUrl,
                        token = data.token
                    )
                )
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun leaveRoom(roomId: String): Result<Unit> {
        return try {
            val response = apiService.leaveRoom(roomId)
            if (!response.success) {
                Result.failure(Exception(response.error ?: "Failed to leave room"))
            } else {
                Result.success(Unit)
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun getRoomMessages(roomId: String): Result<List<RoomMessage>> {
        return try {
            val response = apiService.getRoomMessages(roomId)
            val messages = response.data
            if (!response.success || messages == null) {
                Result.failure(Exception(response.error ?: "Failed to load messages"))
            } else {
                Result.success(messages.map { it.toDomain() })
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun sendRoomMessage(roomId: String, message: String): Result<RoomMessage> {
        return try {
            val response = apiService.sendRoomMessage(roomId, RoomMessageRequest(message))
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "Failed to send message"))
            } else {
                Result.success(data.toDomain())
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun getRoomGifts(roomId: String): Result<List<GiftLog>> {
        return try {
            val response = apiService.getRoomGifts(roomId)
            val gifts = response.data
            if (!response.success || gifts == null) {
                Result.failure(Exception(response.error ?: "Failed to load gifts"))
            } else {
                Result.success(gifts.map { it.toDomain() })
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun sendGift(roomId: String, amount: Long, recipientId: String?): Result<GiftLog> {
        return try {
            val response = apiService.sendGift(roomId, GiftSendRequest(recipientId = recipientId, amount = amount))
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "Failed to send gift"))
            } else {
                Result.success(data.toDomain())
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }
}
