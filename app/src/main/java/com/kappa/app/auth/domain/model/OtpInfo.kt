package com.kappa.app.auth.domain.model

data class OtpInfo(
    val phone: String,
    val code: String,
    val expiresAt: Long
)
