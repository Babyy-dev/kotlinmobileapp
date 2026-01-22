package com.kappa.backend.config

import com.kappa.backend.data.Agencies
import com.kappa.backend.data.CoinWallets
import com.kappa.backend.data.CoinTransactions
import com.kappa.backend.data.PhoneOtps
import com.kappa.backend.data.RefreshTokens
import com.kappa.backend.data.RoomBans
import com.kappa.backend.data.RoomGifts
import com.kappa.backend.data.RoomMessages
import com.kappa.backend.data.RoomParticipants
import com.kappa.backend.data.RoomSeats
import com.kappa.backend.data.Rooms
import com.kappa.backend.data.SeedData
import com.kappa.backend.data.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: AppConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.dbUrl
            driverClassName = config.dbDriver
            username = config.dbUser
            password = config.dbPassword
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users,
                Agencies,
                CoinWallets,
                CoinTransactions,
                PhoneOtps,
                Rooms,
                RoomSeats,
                RoomParticipants,
                RoomBans,
                RoomMessages,
                RoomGifts,
                RefreshTokens
            )
            SeedData.seedIfEmpty()
        }
    }
}
