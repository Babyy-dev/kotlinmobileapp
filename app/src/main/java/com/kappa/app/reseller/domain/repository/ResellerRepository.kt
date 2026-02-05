package com.kappa.app.reseller.domain.repository

import com.kappa.app.reseller.domain.model.ResellerPaymentProof
import com.kappa.app.reseller.domain.model.ResellerSale
import com.kappa.app.reseller.domain.model.ResellerSeller
import com.kappa.app.reseller.domain.model.ResellerSellerLimit
import com.kappa.app.reseller.domain.model.ResellerSendCoinsResult

interface ResellerRepository {
    suspend fun listSellers(): Result<List<ResellerSeller>>
    suspend fun addSeller(sellerId: String): Result<ResellerSeller>
    suspend fun getSellerLimit(sellerId: String): Result<ResellerSellerLimit?>
    suspend fun setSellerLimit(sellerId: String, totalLimit: Long, dailyLimit: Long): Result<ResellerSellerLimit>
    suspend fun listSales(): Result<List<ResellerSale>>
    suspend fun createSale(
        saleId: String,
        sellerId: String,
        buyerId: String,
        amount: Long,
        currency: String,
        destinationAccount: String
    ): Result<ResellerSale>
    suspend fun listProofs(): Result<List<ResellerPaymentProof>>
    suspend fun createProof(
        uri: String,
        amount: Long,
        date: String,
        beneficiary: String,
        note: String?
    ): Result<ResellerPaymentProof>
    suspend fun sendCoins(recipientId: String, amount: Long, note: String?): Result<ResellerSendCoinsResult>
}
