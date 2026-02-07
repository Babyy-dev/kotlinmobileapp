package com.kappa.app.core.livekit

import io.livekit.android.room.Room
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object LiveKitRoomStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _dataEvents = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val dataEvents: SharedFlow<String> = _dataEvents

    private var currentRoom: Room? = null
    private var eventsJob: Job? = null

    fun attachRoom(room: Room) {
        if (currentRoom === room) return
        detachRoom()
        currentRoom = room
        eventsJob = scope.launch {
            room.events.collect { event ->
                if (event is RoomEvent.DataReceived) {
                    val message = runCatching { event.data.toString(Charsets.UTF_8) }.getOrNull() ?: return@collect
                    _dataEvents.emit(message)
                }
            }
        }
    }

    fun detachRoom() {
        eventsJob?.cancel()
        eventsJob = null
        currentRoom = null
    }
}
