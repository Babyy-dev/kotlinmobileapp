package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.home.SearchResult
import javax.inject.Inject

class SearchAllUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(query: String): Result<SearchResult> {
        return audioRepository.search(query)
    }
}
