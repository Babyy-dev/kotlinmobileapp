package com.kappa.app.domain.home

import com.kappa.app.domain.audio.AudioRoom
import com.kappa.app.domain.user.User

data class HomeBanner(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val actionType: String? = null,
    val actionTarget: String? = null
)

data class MiniGame(
    val id: String,
    val title: String,
    val description: String,
    val entryFee: Long,
    val iconUrl: String? = null
)

data class AgencySummary(
    val id: String,
    val name: String,
    val country: String? = null
)

data class SearchResult(
    val rooms: List<AudioRoom> = emptyList(),
    val users: List<User> = emptyList(),
    val agencies: List<AgencySummary> = emptyList()
)
