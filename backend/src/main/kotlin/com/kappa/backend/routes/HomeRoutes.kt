package com.kappa.backend.routes

import com.kappa.backend.data.HomeBanners
import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.HomeBanner
import com.kappa.backend.models.MiniGame
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.homeRoutes() {
    get("home/banners") {
        val banners = transaction {
            HomeBanners
                .select { HomeBanners.isActive eq true }
                .orderBy(HomeBanners.sortOrder to SortOrder.ASC, HomeBanners.updatedAt to SortOrder.DESC)
                .map { row ->
                    HomeBanner(
                        id = row[HomeBanners.id].toString(),
                        title = row[HomeBanners.title],
                        subtitle = row[HomeBanners.subtitle],
                        imageUrl = row[HomeBanners.imageUrl],
                        actionType = row[HomeBanners.actionType],
                        actionTarget = row[HomeBanners.actionTarget],
                        sortOrder = row[HomeBanners.sortOrder],
                        isActive = row[HomeBanners.isActive]
                    )
                }
        }
        call.respond(ApiResponse(success = true, data = banners))
    }

    get("home/mini-games") {
        val games = listOf(
            MiniGame("lucky_draw", "Lucky Draw", "Spin and win rewards", 200),
            MiniGame("battle_arena", "Battle Arena", "Score more in 30 seconds", 300),
            MiniGame("gift_rush", "Gift Rush", "Send gifts to climb the rank", 500)
        )
        call.respond(ApiResponse(success = true, data = games))
    }
}
