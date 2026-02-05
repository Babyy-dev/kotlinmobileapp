package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.home.HomeBanner
import javax.inject.Inject

class GetHomeBannersUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(): Result<List<HomeBanner>> {
        return audioRepository.getHomeBanners()
    }
}
