package com.kappa.backend.services

import com.kappa.backend.data.InboxMessages
import com.kappa.backend.data.InboxThreadReads
import com.kappa.backend.data.InboxThreads
import com.kappa.backend.data.Users
import com.kappa.backend.models.UserRole
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

class SystemMessageService {
    fun sendSystemMessage(recipientId: UUID, message: String) {
        val trimmed = message.trim()
        if (trimmed.isBlank()) return
        transaction {
            val systemId = ensureSystemUser()
            val threadId = findOrCreateThread(systemId, recipientId)
            val now = System.currentTimeMillis()

            InboxThreads.update({ InboxThreads.id eq threadId }) {
                it[InboxThreads.lastMessage] = trimmed
                it[InboxThreads.updatedAt] = now
            }

            InboxMessages.insert {
                it[id] = UUID.randomUUID()
                it[InboxMessages.threadId] = threadId
                it[InboxMessages.senderId] = systemId
                it[InboxMessages.message] = trimmed
                it[InboxMessages.createdAt] = now
            }

            val existingRead = InboxThreadReads.select {
                (InboxThreadReads.threadId eq threadId) and (InboxThreadReads.userId eq systemId)
            }.singleOrNull()
            if (existingRead == null) {
                InboxThreadReads.insert {
                    it[InboxThreadReads.threadId] = threadId
                    it[InboxThreadReads.userId] = systemId
                    it[InboxThreadReads.lastReadAt] = now
                }
            } else {
                InboxThreadReads.update(
                    { (InboxThreadReads.threadId eq threadId) and (InboxThreadReads.userId eq systemId) }
                ) {
                    it[InboxThreadReads.lastReadAt] = now
                }
            }
        }
    }

    private fun ensureSystemUser(): UUID {
        val existing = Users.select { Users.username eq "system" }.singleOrNull()
        if (existing != null) {
            return existing[Users.id]
        }
        val now = System.currentTimeMillis()
        val systemId = UUID.randomUUID()
        Users.insert {
            it[Users.id] = systemId
            it[Users.username] = "system"
            it[Users.email] = "system@kappa.local"
            it[Users.passwordHash] = BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt())
            it[Users.role] = UserRole.USER.name
            it[Users.status] = "active"
            it[Users.isGuest] = false
            it[Users.createdAt] = now
        }
        return systemId
    }

    private fun findOrCreateThread(userA: UUID, userB: UUID): UUID {
        val existing = InboxThreads.select {
            ((InboxThreads.userA eq userA) and (InboxThreads.userB eq userB)) or
                ((InboxThreads.userA eq userB) and (InboxThreads.userB eq userA))
        }.singleOrNull()
        if (existing != null) {
            return existing[InboxThreads.id]
        }
        val threadId = UUID.randomUUID()
        InboxThreads.insert {
            it[InboxThreads.id] = threadId
            it[InboxThreads.userA] = userA
            it[InboxThreads.userB] = userB
            it[InboxThreads.lastMessage] = null
            it[InboxThreads.updatedAt] = System.currentTimeMillis()
        }
        return threadId
    }
}
