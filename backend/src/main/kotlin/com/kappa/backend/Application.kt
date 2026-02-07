package com.kappa.backend

import com.kappa.backend.config.DatabaseFactory
import com.kappa.backend.config.loadConfig
import com.kappa.backend.models.ApiResponse
import com.kappa.backend.routes.agencyRoutes
import com.kappa.backend.routes.authRoutes
import com.kappa.backend.routes.economyRoutes
import com.kappa.backend.routes.gameRoutes
import com.kappa.backend.routes.homeRoutes
import com.kappa.backend.routes.roomRoutes
import com.kappa.backend.routes.socialRoutes
import com.kappa.backend.routes.adminRoutes
import com.kappa.backend.routes.resellerRoutes
import com.kappa.backend.routes.userRoutes
import com.kappa.backend.services.AuthService
import com.kappa.backend.services.AgencyService
import com.kappa.backend.services.EconomyService
import com.kappa.backend.services.GameSessionRegistry
import com.kappa.backend.services.GooglePlayBillingService
import com.kappa.backend.services.GameRealtimeService
import com.kappa.backend.services.LiveKitRoomService
import com.kappa.backend.services.LiveKitTokenService
import com.kappa.backend.services.RoomInteractionService
import com.kappa.backend.services.RoomService
import com.kappa.backend.services.ResellerService
import com.kappa.backend.services.SlotService
import com.kappa.backend.services.SystemMessageService
import com.kappa.backend.services.TokenService
import com.kappa.backend.services.TwilioSmsService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.io.File

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = loadConfig()
    DatabaseFactory.init(config)

    val smsService = TwilioSmsService(config)
    val authService = AuthService(config, smsService)
    val systemMessageService = SystemMessageService()
    val googlePlayBillingService = GooglePlayBillingService(config)
    val economyService = EconomyService(googlePlayBillingService, systemMessageService)
    val gameSessionRegistry = GameSessionRegistry()
    val slotService = SlotService()
    val agencyService = AgencyService(systemMessageService)
    val resellerService = ResellerService(economyService)
    val tokenService = TokenService(config)
    val liveKitTokenService = LiveKitTokenService(config)
    val roomService = RoomService(config, liveKitTokenService)
    val roomInteractionService = RoomInteractionService()
    val liveKitRoomService = LiveKitRoomService(config, liveKitTokenService)
    val gameRealtimeService = GameRealtimeService(gameSessionRegistry, economyService, liveKitRoomService)

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                isLenient = true
            }
        )
    }

    install(CORS) {
        anyHost()
        allowHeader("Authorization")
        allowHeader("Content-Type")
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, error = cause.message ?: "Invalid request")
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Unit>(success = false, error = cause.message ?: "Unexpected error")
            )
        }
    }

    install(Authentication) {
        jwt {
            realm = config.jwtRealm
            verifier(tokenService.verifier())
            validate { credential ->
                if (credential.payload.subject != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    routing {
        get("/admin") {
            call.respondRedirect("/admin/index.html")
        }
        staticFiles("/uploads", File("uploads"))
        staticResources("/admin", "admin")
        route("api") {
            authRoutes(authService)
            authenticate {
                userRoutes(authService)
                economyRoutes(economyService, slotService)
                roomRoutes(roomService, authService, roomInteractionService)
                gameRoutes(roomService, gameSessionRegistry, gameRealtimeService)
                homeRoutes()
                socialRoutes(systemMessageService)
                agencyRoutes(agencyService)
                adminRoutes()
                resellerRoutes(resellerService)
            }
        }
    }

    // LiveKit data channel used for realtime updates; no Socket.IO server.
}
