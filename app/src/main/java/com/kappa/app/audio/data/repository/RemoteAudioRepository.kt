package com.kappa.app.audio.data.repository

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.audio.domain.repository.JoinRoomInfo
import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.ErrorMapper
import com.kappa.app.core.network.model.toDomain
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
            val response = apiService.joinRoom(roomId)
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
}
