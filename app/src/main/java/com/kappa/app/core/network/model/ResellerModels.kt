package com.kappa.app.core.network.model

data class ResellerSellerRequestDto(
    val sellerId: String
)

data class ResellerSellerDto(
    val id: String,
    val sellerId: String,
    val createdAt: Long
)

data class ResellerSellerLimitRequestDto(
    val totalLimit: Long,
    val dailyLimit: Long
)

data class ResellerSellerLimitDto(
    val sellerId: String,
    val totalLimit: Long,
    val dailyLimit: Long,
    val updatedAt: Long
)

data class ResellerSaleRequestDto(
    val saleId: String,
    val sellerId: String,
    val buyerId: String,
    val amount: Long,
    val currency: String,
    val destinationAccount: String
)

data class ResellerSaleDto(
    val id: String,
    val saleId: String?,
    val sellerId: String,
    val buyerId: String,
    val amount: Long,
    val currency: String,
    val destinationAccount: String,
    val createdAt: Long
)

data class ResellerProofRequestDto(
    val uri: String,
    val amount: Long,
    val date: String,
    val beneficiary: String,
    val note: String? = null
)

data class ResellerProofDto(
    val id: String,
    val uri: String,
    val amount: Long,
    val date: String,
    val beneficiary: String,
    val note: String? = null,
    val createdAt: Long
)

data class ResellerSendCoinsRequestDto(
    val recipientId: String,
    val amount: Long,
    val note: String? = null
)

data class ResellerSendCoinsResponseDto(
    val balance: Long
)
