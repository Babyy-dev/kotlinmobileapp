package com.kappa.backend.config

import com.kappa.backend.data.Agencies
import com.kappa.backend.data.CoinWallets
import com.kappa.backend.data.CoinPackages
import com.kappa.backend.data.CoinPurchases
import com.kappa.backend.data.CoinTransactions
import com.kappa.backend.data.DiamondConversions
import com.kappa.backend.data.DiamondTransactions
import com.kappa.backend.data.DiamondWallets
import com.kappa.backend.data.GiftTransactions
import com.kappa.backend.data.Gifts
import com.kappa.backend.data.AgencyCommissions
import com.kappa.backend.data.AgencyApplications
import com.kappa.backend.data.ResellerApplications
import com.kappa.backend.data.ResellerSellers
import com.kappa.backend.data.ResellerSellerLimits
import com.kappa.backend.data.ResellerSales
import com.kappa.backend.data.ResellerPaymentProofs
import com.kappa.backend.data.ResellerAuditLogs
import com.kappa.backend.data.AgencyAuditLogs
import com.kappa.backend.data.RewardRequests
import com.kappa.backend.data.SlotPlays
import com.kappa.backend.data.TeamGroups
import com.kappa.backend.data.TeamMembers
import com.kappa.backend.data.Announcements
import com.kappa.backend.data.PhoneOtps
import com.kappa.backend.data.RefreshTokens
import com.kappa.backend.data.RoomBans
import com.kappa.backend.data.RoomGifts
import com.kappa.backend.data.RoomMessages
import com.kappa.backend.data.RoomParticipants
import com.kappa.backend.data.RoomSeats
import com.kappa.backend.data.Rooms
import com.kappa.backend.data.Families
import com.kappa.backend.data.FamilyMembers
import com.kappa.backend.data.Friends
import com.kappa.backend.data.InboxMessages
import com.kappa.backend.data.InboxThreads
import com.kappa.backend.data.InboxThreadReads
import com.kappa.backend.data.AdminGlobalConfigs
import com.kappa.backend.data.AdminGameConfigs
import com.kappa.backend.data.AdminUserConfigs
import com.kappa.backend.data.AdminQualificationConfigs
import com.kappa.backend.data.AdminLockRules
import com.kappa.backend.data.AdminAuditLogs
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
                AgencyApplications,
                ResellerApplications,
                ResellerSellers,
                ResellerSellerLimits,
                ResellerSales,
                ResellerPaymentProofs,
                ResellerAuditLogs,
                AgencyAuditLogs,
                TeamGroups,
                TeamMembers,
                CoinWallets,
                CoinTransactions,
                CoinPackages,
                CoinPurchases,
                DiamondWallets,
                DiamondTransactions,
                DiamondConversions,
                RewardRequests,
                Gifts,
                GiftTransactions,
                AgencyCommissions,
                SlotPlays,
                Announcements,
                PhoneOtps,
                Rooms,
                RoomSeats,
                RoomParticipants,
                RoomBans,
                RoomMessages,
                RoomGifts,
                RefreshTokens,
                Families,
                FamilyMembers,
                Friends,
                InboxThreads,
                InboxMessages,
                InboxThreadReads,
                AdminGlobalConfigs,
                AdminGameConfigs,
                AdminUserConfigs,
                AdminQualificationConfigs,
                AdminLockRules,
                AdminAuditLogs
            )
            SeedData.seedIfEmpty()
        }
    }
}
