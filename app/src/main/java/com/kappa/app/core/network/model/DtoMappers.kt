package com.kappa.app.core.network.model

import com.kappa.app.auth.domain.model.OtpInfo
import com.kappa.app.domain.audio.AudioRoom
import com.kappa.app.domain.audio.GiftLog
import com.kappa.app.domain.audio.RoomMessage
import com.kappa.app.domain.audio.SeatMode
import com.kappa.app.domain.audio.RoomSeat
import com.kappa.app.domain.audio.SeatStatus
import com.kappa.app.domain.economy.CoinBalance
import com.kappa.app.domain.economy.Transaction
import com.kappa.app.domain.economy.TransactionType
import com.kappa.app.domain.user.Role
import com.kappa.app.domain.user.User
import com.kappa.app.domain.home.HomeBanner
import com.kappa.app.domain.home.MiniGame
import com.kappa.app.domain.home.AgencySummary
import com.kappa.app.domain.home.SearchResult

fun UserDto.toDomain(): User {
    val resolvedRole = Role.fromApi(role)
    return User(
        id = id,
        username = username,
        email = email,
        role = resolvedRole,
        phone = phone,
        nickname = nickname,
        avatarUrl = avatarUrl,
        country = country,
        language = language,
        isGuest = isGuest,
        permissions = emptyList()
    )
}

fun CoinBalanceDto.toDomain(): CoinBalance {
    return CoinBalance(
        userId = userId,
        balance = balance,
        currency = currency
    )
}

fun CoinTransactionDto.toDomain(): Transaction {
    val resolvedType = when (type.uppercase()) {
        "PURCHASE", "PURCHASE_GOOGLE" -> TransactionType.PURCHASE
        "REWARD" -> TransactionType.REWARD
        "DEBIT", "GIFT" -> TransactionType.WITHDRAWAL
        else -> TransactionType.DEPOSIT
    }
    return Transaction(
        id = id,
        userId = userId,
        amount = if (resolvedType == TransactionType.WITHDRAWAL) -amount else amount,
        type = resolvedType,
        timestamp = createdAt,
        description = type
    )
}

fun PhoneOtpResponse.toDomain(): OtpInfo {
    return OtpInfo(
        phone = phone,
        code = code,
        expiresAt = expiresAt
    )
}

fun RoomDto.toDomain(): AudioRoom {
    val resolvedSeatMode = runCatching { SeatMode.valueOf(seatMode) }.getOrDefault(SeatMode.FREE)
    return AudioRoom(
        id = id,
        name = name,
        isActive = isActive,
        seatMode = resolvedSeatMode,
        participantCount = participantCount,
        maxSeats = maxSeats,
        requiresPassword = requiresPassword,
        country = country,
        region = region,
        agencyName = agencyName,
        agencyIconUrl = agencyIconUrl,
        roomCode = roomCode,
        isFavorite = isFavorite
    )
}

fun RoomSeatDto.toDomain(): RoomSeat {
    val resolvedStatus = runCatching { SeatStatus.valueOf(status) }.getOrDefault(SeatStatus.FREE)
    return RoomSeat(
        seatNumber = seatNumber,
        status = resolvedStatus,
        userId = userId,
        username = username
    )
}

fun RoomMessageDto.toDomain(): RoomMessage {
    return RoomMessage(
        id = id,
        userId = userId,
        username = username,
        message = message,
        createdAt = createdAt,
        messageType = type
    )
}

fun GiftSendDto.toDomain(): GiftLog {
    return GiftLog(
        id = id,
        senderId = senderId,
        recipientId = recipientId,
        amount = amount,
        senderBalance = senderBalance,
        createdAt = createdAt
    )
}

fun AgencyApplicationDto.toDomain(): com.kappa.app.agency.domain.model.AgencyApplication {
    return com.kappa.app.agency.domain.model.AgencyApplication(
        id = id,
        agencyName = agencyName,
        status = status,
        createdAt = createdAt,
        reviewedAt = reviewedAt
    )
}

fun ResellerApplicationDto.toDomain(): com.kappa.app.agency.domain.model.ResellerApplication {
    return com.kappa.app.agency.domain.model.ResellerApplication(
        id = id,
        status = status,
        createdAt = createdAt,
        reviewedAt = reviewedAt
    )
}

fun TeamDto.toDomain(): com.kappa.app.agency.domain.model.Team {
    return com.kappa.app.agency.domain.model.Team(
        id = id,
        name = name,
        ownerUserId = ownerUserId,
        agencyId = agencyId
    )
}

fun AgencyCommissionDto.toDomain(): com.kappa.app.agency.domain.model.AgencyCommission {
    return com.kappa.app.agency.domain.model.AgencyCommission(
        id = id,
        diamondsAmount = diamondsAmount,
        commissionUsd = commissionUsd,
        createdAt = createdAt
    )
}

fun AgencyRoomDto.toRow(): Pair<String, String> {
    return name to status
}

fun AgencyHostDto.toRow(): Pair<String, String> {
    return name to "Diamonds: $diamonds"
}

fun ResellerSellerDto.toDomain(): com.kappa.app.reseller.domain.model.ResellerSeller {
    return com.kappa.app.reseller.domain.model.ResellerSeller(
        id = id,
        sellerId = sellerId,
        createdAt = createdAt
    )
}

fun ResellerSellerLimitDto.toDomain(): com.kappa.app.reseller.domain.model.ResellerSellerLimit {
    return com.kappa.app.reseller.domain.model.ResellerSellerLimit(
        sellerId = sellerId,
        totalLimit = totalLimit,
        dailyLimit = dailyLimit,
        updatedAt = updatedAt
    )
}

fun ResellerSaleDto.toDomain(): com.kappa.app.reseller.domain.model.ResellerSale {
    return com.kappa.app.reseller.domain.model.ResellerSale(
        id = id,
        saleId = saleId,
        sellerId = sellerId,
        buyerId = buyerId,
        amount = amount,
        currency = currency,
        destinationAccount = destinationAccount,
        createdAt = createdAt
    )
}

fun ResellerProofDto.toDomain(): com.kappa.app.reseller.domain.model.ResellerPaymentProof {
    return com.kappa.app.reseller.domain.model.ResellerPaymentProof(
        id = id,
        uri = uri,
        amount = amount,
        date = date,
        beneficiary = beneficiary,
        note = note,
        createdAt = createdAt
    )
}

fun GiftCatalogDto.toDomain(): com.kappa.app.domain.audio.GiftCatalogItem {
    return com.kappa.app.domain.audio.GiftCatalogItem(
        id = id,
        name = name,
        giftType = giftType,
        costCoins = costCoins,
        diamondPercent = diamondPercent,
        category = category,
        imageUrl = imageUrl
    )
}

fun HomeBannerDto.toDomain(): HomeBanner {
    return HomeBanner(
        id = id,
        title = title,
        subtitle = subtitle,
        imageUrl = imageUrl,
        actionType = actionType,
        actionTarget = actionTarget
    )
}

fun MiniGameDto.toDomain(): MiniGame {
    return MiniGame(
        id = id,
        title = title,
        description = description,
        entryFee = entryFee,
        iconUrl = iconUrl
    )
}

fun AgencySummaryDto.toDomain(): AgencySummary {
    return AgencySummary(
        id = id,
        name = name,
        country = country
    )
}

fun SearchResultDto.toDomain(): SearchResult {
    return SearchResult(
        rooms = rooms.map { it.toDomain() },
        users = users.map { it.toDomain() },
        agencies = agencies.map { it.toDomain() }
    )
}
