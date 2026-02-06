package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class HomeBanner(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String,
    val actionType: String,
    val actionTarget: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)

@Serializable
data class MiniGame(
    val id: String,
    val title: String,
    val description: String,
    val entryFee: Long
)

@Serializable
data class BannerUploadResponse(
    val url: String
)
