package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.audio.GiftCatalogItem
import javax.inject.Inject

class GetGiftCatalogUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(): Result<List<GiftCatalogItem>> {
        return audioRepository.getGiftCatalog()
    }
}
