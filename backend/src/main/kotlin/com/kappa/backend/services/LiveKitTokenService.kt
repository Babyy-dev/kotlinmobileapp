package com.kappa.backend.services

import com.kappa.backend.config.AppConfig
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.Date

class LiveKitTokenService(private val config: AppConfig) {
    fun generateToken(identity: String, name: String, roomName: String): String {
        val now = Date()
        val expiresAt = Date(now.time + config.livekitTokenTtlSeconds * 1000)
        val claims = JWTClaimsSet.Builder()
            .issuer(config.livekitApiKey)
            .subject(identity)
            .issueTime(now)
            .expirationTime(expiresAt)
            .claim("name", name)
            .claim(
                "video",
                mapOf(
                    "room" to roomName,
                    "roomJoin" to true,
                    "canPublish" to true,
                    "canSubscribe" to true,
                    "canPublishData" to true
                )
            )
            .build()

        val signer = MACSigner(config.livekitApiSecret)
        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claims)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }

    fun generateServerToken(roomName: String): String {
        val now = Date()
        val expiresAt = Date(now.time + config.livekitTokenTtlSeconds * 1000)
        val claims = JWTClaimsSet.Builder()
            .issuer(config.livekitApiKey)
            .subject("kappa-backend")
            .issueTime(now)
            .expirationTime(expiresAt)
            .claim(
                "video",
                mapOf(
                    "room" to roomName,
                    "roomAdmin" to true,
                    "canPublish" to true,
                    "canSubscribe" to true,
                    "canPublishData" to true
                )
            )
            .build()
        val signer = MACSigner(config.livekitApiSecret)
        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claims)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }
}
