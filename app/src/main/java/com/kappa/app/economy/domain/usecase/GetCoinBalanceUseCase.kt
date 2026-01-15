package com.kappa.app.economy.domain.usecase

import com.kappa.app.domain.economy.CoinBalance
import com.kappa.app.economy.domain.repository.EconomyRepository
import javax.inject.Inject

/**
 * UseCase for getting coin balance.
 */
class GetCoinBalanceUseCase @Inject constructor(
    private val economyRepository: EconomyRepository
) {
    suspend operator fun invoke(userId: String): Result<CoinBalance> {
        return economyRepository.getCoinBalance(userId)
    }
}
