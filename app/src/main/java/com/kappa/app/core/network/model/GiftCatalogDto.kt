package com.kappa.app.core.network.model

data class GiftCatalogDto(
    val id: String,
    val name: String,
    val giftType: String,
    val costCoins: Long,
    val diamondPercent: Int,
    val isActive: Boolean,
    val category: String? = null,
    val imageUrl: String? = null
)
