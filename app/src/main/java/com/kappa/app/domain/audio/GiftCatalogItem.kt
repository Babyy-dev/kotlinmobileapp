package com.kappa.app.domain.audio

data class GiftCatalogItem(
    val id: String,
    val name: String,
    val giftType: String,
    val costCoins: Long,
    val diamondPercent: Int,
    val category: String? = null,
    val imageUrl: String? = null
)
