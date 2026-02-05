package com.kappa.app.reseller.domain.model

data class ResellerSeller(
    val id: String,
    val sellerId: String,
    val createdAt: Long
)

data class ResellerSellerLimit(
    val sellerId: String,
    val totalLimit: Long,
    val dailyLimit: Long,
    val updatedAt: Long
)

data class ResellerSale(
    val id: String,
    val saleId: String?,
    val sellerId: String,
    val buyerId: String,
    val amount: Long,
    val currency: String,
    val destinationAccount: String,
    val createdAt: Long
)

data class ResellerPaymentProof(
    val id: String,
    val uri: String,
    val amount: Long,
    val date: String,
    val beneficiary: String,
    val note: String?,
    val createdAt: Long
)

data class ResellerSendCoinsResult(
    val balance: Long
)
