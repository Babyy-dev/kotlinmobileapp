package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.home.MiniGame
import javax.inject.Inject

class GetPopularMiniGamesUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(): Result<List<MiniGame>> {
        return audioRepository.getPopularMiniGames()
    }
}
