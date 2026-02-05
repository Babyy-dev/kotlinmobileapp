package com.kappa.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.model.FamilyDto
import com.kappa.app.core.network.model.FamilyMemberDto
import com.kappa.app.core.network.model.FamilyRoomDto
import com.kappa.app.core.network.model.InboxThreadDto
import com.kappa.app.core.network.model.FriendDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

data class HomeViewState(
    val inbox: List<InboxItem> = emptyList(),
    val friends: List<InboxItem> = emptyList(),
    val friendSearch: List<InboxItem> = emptyList(),
    val family: FamilyDto? = null,
    val familyName: String? = null,
    val familyCode: String? = null,
    val familyMembers: List<Pair<String, String>> = emptyList(),
    val familyRooms: List<Pair<String, String>> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {
    private val _viewState = MutableStateFlow(HomeViewState())
    val viewState: StateFlow<HomeViewState> = _viewState.asStateFlow()
    private var searchJob: Job? = null
    private var lastSearch: String = ""

    fun loadAll() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, message = null)
            val threads = runCatching { apiService.getInboxThreads() }.getOrNull()
            val friends = runCatching { apiService.getFriends() }.getOrNull()
            val family = runCatching { apiService.getMyFamily() }.getOrNull()

            val inboxItems = threads?.data?.map { it.toInboxItem() }.orEmpty()
            val friendItems = friends?.data?.map { it.toFriendItem() }.orEmpty()
            val familyData = family?.data
            val members = if (familyData != null) {
                runCatching { apiService.getFamilyMembers(familyData.id) }.getOrNull()
                    ?.data?.map { it.toRow() }.orEmpty()
            } else {
                emptyList()
            }
            val rooms = if (familyData != null) {
                runCatching { apiService.getFamilyRooms(familyData.id) }.getOrNull()
                    ?.data?.map { it.toRow() }.orEmpty()
            } else {
                emptyList()
            }

            _viewState.value = _viewState.value.copy(
                inbox = inboxItems,
                friends = friendItems,
                friendSearch = friendItems,
                family = familyData,
                familyName = familyData?.name,
                familyCode = familyData?.code,
                familyMembers = members,
                familyRooms = rooms,
                isLoading = false
            )
        }
    }

    fun searchFriends(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _viewState.value = _viewState.value.copy(friendSearch = _viewState.value.friends)
            return
        }
        if (trimmed == lastSearch) return
        lastSearch = trimmed
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            val response = runCatching { apiService.searchFriends(trimmed) }.getOrNull()
            val results = response?.data?.map { it.toFriendItem() }.orEmpty()
            _viewState.value = _viewState.value.copy(friendSearch = results)
        }
    }

    fun createFamily(name: String) {
        if (name.isBlank()) {
            _viewState.value = _viewState.value.copy(message = "Enter a family name")
            return
        }
        if (_viewState.value.family != null) {
            _viewState.value = _viewState.value.copy(message = "Already in a family")
            return
        }
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, message = null)
            val response = runCatching { apiService.createFamily(com.kappa.app.core.network.model.FamilyCreateRequest(name)) }
                .getOrNull()
            if (response == null || !response.success || response.data == null) {
                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                    message = response?.error ?: "Unable to create family"
                )
                return@launch
            }
            loadAll()
        }
    }

    fun joinFamily(code: String) {
        val trimmed = code.trim()
        if (trimmed.isBlank()) {
            _viewState.value = _viewState.value.copy(message = "Enter a family code")
            return
        }
        if (_viewState.value.family != null) {
            _viewState.value = _viewState.value.copy(message = "Already in a family")
            return
        }
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, message = null)
            val response = runCatching { apiService.joinFamily(com.kappa.app.core.network.model.FamilyJoinRequest(trimmed)) }
                .getOrNull()
            if (response == null || !response.success || response.data == null) {
                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                    message = response?.error ?: "Unable to join family"
                )
                return@launch
            }
            loadAll()
        }
    }

    fun createFamilyRoom(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            _viewState.value = _viewState.value.copy(message = "Enter a room name")
            return
        }
        val familyId = _viewState.value.family?.id
        if (familyId.isNullOrBlank()) {
            _viewState.value = _viewState.value.copy(message = "Join a family first")
            return
        }
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, message = null)
            val response = runCatching {
                apiService.createFamilyRoom(
                    familyId,
                    com.kappa.app.core.network.model.RoomCreateRequest(name = trimmed)
                )
            }.getOrNull()
            if (response == null || !response.success || response.data == null) {
                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                    message = response?.error ?: "Unable to create family room"
                )
                return@launch
            }
            loadAll()
        }
    }

    fun clearMessage() {
        _viewState.value = _viewState.value.copy(message = null)
    }

    fun markThreadRead(threadId: String) {
        viewModelScope.launch {
            runCatching { apiService.markInboxThreadRead(threadId) }
            val updated = _viewState.value.inbox.map { item ->
                if (item.id == threadId) item.copy(unreadCount = 0) else item
            }
            _viewState.value = _viewState.value.copy(inbox = updated)
        }
    }

    private fun InboxThreadDto.toInboxItem(): InboxItem {
        return InboxItem(
            id = id,
            name = peerName,
            message = lastMessage ?: "",
            badge = null,
            isOnline = false,
            unreadCount = unreadCount
        )
    }

    private fun FriendDto.toFriendItem(): InboxItem {
        return InboxItem(
            id = userId,
            name = nickname ?: username,
            message = "",
            badge = null,
            isOnline = false
        )
    }

    private fun FamilyMemberDto.toRow(): Pair<String, String> {
        val name = nickname ?: username
        return name to role
    }

    private fun FamilyRoomDto.toRow(): Pair<String, String> {
        return name to status
    }
}
