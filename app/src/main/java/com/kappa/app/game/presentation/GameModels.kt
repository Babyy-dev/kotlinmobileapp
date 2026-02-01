package com.kappa.app.game.presentation

enum class GameType {
    LUCKY_DRAW,
    BATTLE,
    GIFT_RUSH,
    TAP_SPEED
}

data class GameCard(
    val type: GameType,
    val title: String,
    val subtitle: String,
    val entryFee: Long
)

data class GamePlayer(
    val id: String,
    val name: String,
    val score: Int
)
