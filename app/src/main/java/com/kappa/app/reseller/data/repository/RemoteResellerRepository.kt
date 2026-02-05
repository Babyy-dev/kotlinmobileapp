package com.kappa.app.reseller.data.repository

import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.BaseApiResponse
import com.kappa.app.core.network.ErrorMapper
import com.kappa.app.core.network.model.ResellerProofDto
import com.kappa.app.core.network.model.ResellerProofRequestDto
import com.kappa.app.core.network.model.ResellerSaleDto
import com.kappa.app.core.network.model.ResellerSaleRequestDto
import com.kappa.app.core.network.model.ResellerSellerDto
import com.kappa.app.core.network.model.ResellerSellerLimitDto
import com.kappa.app.core.network.model.ResellerSellerLimitRequestDto
import com.kappa.app.core.network.model.ResellerSellerRequestDto
import com.kappa.app.core.network.model.ResellerSendCoinsRequestDto
import com.kappa.app.core.network.model.ResellerSendCoinsResponseDto
import com.kappa.app.core.network.model.toDomain
import com.kappa.app.reseller.domain.model.ResellerPaymentProof
import com.kappa.app.reseller.domain.model.ResellerSale
import com.kappa.app.reseller.domain.model.ResellerSeller
import com.kappa.app.reseller.domain.model.ResellerSellerLimit
import com.kappa.app.reseller.domain.model.ResellerSendCoinsResult
import com.kappa.app.reseller.domain.repository.ResellerRepository
import javax.inject.Inject

class RemoteResellerRepository @Inject constructor(
    private val apiService: ApiService,
    private val errorMapper: ErrorMapper
) : ResellerRepository {

    override suspend fun listSellers(): Result<List<ResellerSeller>> {
        return safeCall<List<ResellerSellerDto>, List<ResellerSeller>>(
            call = { apiService.getResellerSellers() }
        ) { list -> list.map(ResellerSellerDto::toDomain) }
    }

    override suspend fun addSeller(sellerId: String): Result<ResellerSeller> {
        return safeCall<ResellerSellerDto, ResellerSeller>(
            call = { apiService.addResellerSeller(ResellerSellerRequestDto(sellerId)) }
        ) { it.toDomain() }
    }

    override suspend fun getSellerLimit(sellerId: String): Result<ResellerSellerLimit?> {
        return safeCall<ResellerSellerLimitDto?, ResellerSellerLimit?>(
            call = { apiService.getResellerSellerLimits(sellerId) }
        ) { it?.toDomain() }
    }

    override suspend fun setSellerLimit(
        sellerId: String,
        totalLimit: Long,
        dailyLimit: Long
    ): Result<ResellerSellerLimit> {
        return safeCall<ResellerSellerLimitDto, ResellerSellerLimit>(
            call = { apiService.setResellerSellerLimits(sellerId, ResellerSellerLimitRequestDto(totalLimit, dailyLimit)) }
        ) { it.toDomain() }
    }

    override suspend fun listSales(): Result<List<ResellerSale>> {
        return safeCall<List<ResellerSaleDto>, List<ResellerSale>>(
            call = { apiService.getResellerSales() }
        ) { list -> list.map(ResellerSaleDto::toDomain) }
    }

    override suspend fun createSale(
        saleId: String,
        sellerId: String,
        buyerId: String,
        amount: Long,
        currency: String,
        destinationAccount: String
    ): Result<ResellerSale> {
        val request = ResellerSaleRequestDto(
            saleId = saleId,
            sellerId = sellerId,
            buyerId = buyerId,
            amount = amount,
            currency = currency,
            destinationAccount = destinationAccount
        )
        return safeCall<ResellerSaleDto, ResellerSale>(
            call = { apiService.createResellerSale(request) }
        ) { it.toDomain() }
    }

    override suspend fun listProofs(): Result<List<ResellerPaymentProof>> {
        return safeCall<List<ResellerProofDto>, List<ResellerPaymentProof>>(
            call = { apiService.getResellerProofs() }
        ) { list -> list.map(ResellerProofDto::toDomain) }
    }

    override suspend fun createProof(
        uri: String,
        amount: Long,
        date: String,
        beneficiary: String,
        note: String?
    ): Result<ResellerPaymentProof> {
        val request = ResellerProofRequestDto(uri, amount, date, beneficiary, note)
        return safeCall<ResellerProofDto, ResellerPaymentProof>(
            call = { apiService.createResellerProof(request) }
        ) { it.toDomain() }
    }

    override suspend fun sendCoins(
        recipientId: String,
        amount: Long,
        note: String?
    ): Result<ResellerSendCoinsResult> {
        return safeCall<ResellerSendCoinsResponseDto, ResellerSendCoinsResult>(
            call = { apiService.sendResellerCoins(ResellerSendCoinsRequestDto(recipientId, amount, note)) }
        ) { ResellerSendCoinsResult(balance = it.balance) }
    }

    private suspend fun <T, R> safeCall(
        call: suspend () -> BaseApiResponse<T>,
        transform: (T) -> R
    ): Result<R> {
        return try {
            val response = call()
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "Failed to load data"))
            } else {
                Result.success(transform(data))
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }
}
