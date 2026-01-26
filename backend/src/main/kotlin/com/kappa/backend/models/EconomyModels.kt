package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class CoinBalanceResponse(
    val userId: String,
    val balance: Long,
    val currency: String = "coins"
)

@Serializable
data class CoinMutationRequest(
    val userId: String,
    val amount: Long
)

@Serializable
data class CoinMutationResponse(
    val userId: String,
    val balance: Long,
    val currency: String = "coins"
)

@Serializable
data class CoinTransactionResponse(
    val id: String,
    val userId: String,
    val type: String,
    val amount: Long,
    val balanceAfter: Long,
    val createdAt: Long
)

@Serializable
data class DiamondBalanceResponse(
    val userId: String,
    val balance: Long,
    val locked: Long,
    val currency: String = "diamonds"
)

@Serializable
data class DiamondTransactionResponse(
    val id: String,
    val userId: String,
    val type: String,
    val amount: Long,
    val balanceAfter: Long,
    val createdAt: Long
)

@Serializable
data class DiamondConversionRequest(
    val diamonds: Long
)

@Serializable
data class DiamondConversionResponse(
    val userId: String,
    val diamondsUsed: Long,
    val coinsGenerated: Long,
    val coinBalance: Long,
    val diamondBalance: Long,
    val lockedDiamonds: Long
)

@Serializable
data class RewardRequestCreate(
    val diamonds: Long
)

@Serializable
data class RewardRequestResponse(
    val id: String,
    val userId: String,
    val diamonds: Long,
    val status: String,
    val createdAt: Long,
    val processedAt: Long? = null,
    val note: String? = null
)

@Serializable
data class CoinPackageResponse(
    val id: String,
    val name: String,
    val coinAmount: Long,
    val priceUsd: String,
    val isActive: Boolean
)

@Serializable
data class CoinPackageRequest(
    val name: String,
    val coinAmount: Long,
    val priceUsd: String,
    val isActive: Boolean = true
)

@Serializable
data class CoinPurchaseRequest(
    val packageId: String,
    val provider: String,
    val providerTxId: String
)

@Serializable
data class CoinPurchaseResponse(
    val id: String,
    val status: String,
    val coinBalance: Long
)

@Serializable
data class SlotPlayRequest(
    val betCoins: Long
)

@Serializable
data class SlotPlayResponse(
    val id: String,
    val betCoins: Long,
    val winCoins: Long,
    val balanceAfter: Long,
    val createdAt: Long
)

@Serializable
data class GiftCatalogResponse(
    val id: String,
    val name: String,
    val giftType: String,
    val costCoins: Long,
    val diamondPercent: Int,
    val isActive: Boolean
)

@Serializable
data class GiftCreateRequest(
    val name: String,
    val giftType: String,
    val costCoins: Long,
    val diamondPercent: Int
)

@Serializable
data class GiftUpdateRequest(
    val name: String? = null,
    val giftType: String? = null,
    val costCoins: Long? = null,
    val diamondPercent: Int? = null,
    val isActive: Boolean? = null
)

@Serializable
data class AnnouncementResponse(
    val id: String,
    val title: String,
    val message: String,
    val isActive: Boolean
)

@Serializable
data class AnnouncementRequest(
    val title: String,
    val message: String,
    val isActive: Boolean = true
)

@Serializable
data class AgencyCommissionResponse(
    val id: String,
    val agencyId: String,
    val userId: String,
    val diamondsAmount: Long,
    val commissionUsd: String,
    val createdAt: Long
)
