package com.kappa.app.user.domain.usecase

import com.kappa.app.domain.user.User
import com.kappa.app.user.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<User> {
        return userRepository.updateUser(user)
    }
}
