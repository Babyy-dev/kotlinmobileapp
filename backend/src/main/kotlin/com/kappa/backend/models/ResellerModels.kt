package com.kappa.backend.models

data class ResellerSellerRequest(
    val sellerId: String
)

data class ResellerSellerResponse(
    val id: String,
    val sellerId: String,
    val createdAt: Long
)

data class ResellerSellerLimitRequest(
    val totalLimit: Long,
    val dailyLimit: Long
)

data class ResellerSellerLimitResponse(
    val sellerId: String,
    val totalLimit: Long,
    val dailyLimit: Long,
    val updatedAt: Long
)

data class ResellerSaleRequest(
    val saleId: String,
    val sellerId: String,
    val buyerId: String,
    val amount: Long,
    val currency: String,
    val destinationAccount: String
)

data class ResellerSaleResponse(
    val id: String,
    val saleId: String?,
    val sellerId: String,
    val buyerId: String,
    val amount: Long,
    val currency: String,
    val destinationAccount: String,
    val createdAt: Long
)

data class ResellerProofRequest(
    val uri: String,
    val amount: Long,
    val date: String,
    val beneficiary: String,
    val note: String? = null
)

data class ResellerProofResponse(
    val id: String,
    val uri: String,
    val amount: Long,
    val date: String,
    val beneficiary: String,
    val note: String? = null,
    val createdAt: Long
)

data class ResellerSendCoinsRequest(
    val recipientId: String,
    val amount: Long,
    val note: String? = null
)

data class ResellerSendCoinsResponse(
    val balance: Long
)

data class ResellerLogResponse(
    val id: String,
    val resellerId: String,
    val actorId: String,
    val action: String,
    val message: String? = null,
    val createdAt: Long
)
