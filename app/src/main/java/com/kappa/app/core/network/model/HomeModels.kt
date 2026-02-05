package com.kappa.app.core.network.model

data class HomeBannerDto(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val actionType: String? = null,
    val actionTarget: String? = null
)

data class MiniGameDto(
    val id: String,
    val title: String,
    val description: String,
    val entryFee: Long,
    val iconUrl: String? = null
)

data class AgencySummaryDto(
    val id: String,
    val name: String,
    val country: String? = null
)

data class SearchResultDto(
    val rooms: List<RoomDto> = emptyList(),
    val users: List<UserDto> = emptyList(),
    val agencies: List<AgencySummaryDto> = emptyList()
)
