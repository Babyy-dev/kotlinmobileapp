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
                it[lastMessage] = trimmed
                it[updatedAt] = now
            }

            InboxMessages.insert {
                it[id] = UUID.randomUUID()
                it[InboxMessages.threadId] = threadId
                it[InboxMessages.senderId] = systemId
                it[message] = trimmed
                it[createdAt] = now
            }

            val existingRead = InboxThreadReads.select {
                (InboxThreadReads.threadId eq threadId) and (InboxThreadReads.userId eq systemId)
            }.singleOrNull()
            if (existingRead == null) {
                InboxThreadReads.insert {
                    it[InboxThreadReads.threadId] = threadId
                    it[InboxThreadReads.userId] = systemId
                    it[lastReadAt] = now
                }
            } else {
                InboxThreadReads.update(
                    { (InboxThreadReads.threadId eq threadId) and (InboxThreadReads.userId eq systemId) }
                ) {
                    it[lastReadAt] = now
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
            it[id] = systemId
            it[username] = "system"
            it[email] = "system@kappa.local"
            it[passwordHash] = BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt())
            it[Users.role] = UserRole.USER.name
            it[status] = "active"
            it[isGuest] = false
            it[createdAt] = now
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
            it[id] = threadId
            it[InboxThreads.userA] = userA
            it[InboxThreads.userB] = userB
            it[lastMessage] = null
            it[updatedAt] = System.currentTimeMillis()
        }
        return threadId
    }
}
