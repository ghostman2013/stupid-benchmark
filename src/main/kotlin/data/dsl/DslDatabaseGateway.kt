package data.dsl

import data.dsl.tables.MessageTable
import data.dsl.tables.UserTable
import domain.entities.Message
import domain.entities.NewMessage
import domain.entities.NewUser
import domain.entities.User
import domain.gateways.IDatabaseGateway
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

class DslDatabaseGateway(private var url: String): IDatabaseGateway {
    private lateinit var db: Database

    override fun connect() {
        this.db = Database.connect(url)
    }

    override fun disconnect() {
        TransactionManager.closeAndUnregister(this.db)
    }

    override fun createDatabase() {
        transaction {
            SchemaUtils.create(UserTable, MessageTable)
        }
    }

    override fun dropDatabase() {
        transaction {
            SchemaUtils.drop(UserTable, MessageTable)
        }
    }

    override fun addUsers(newUsers: List<NewUser>): List<User> {
        return transaction {
            UserTable.batchInsert(newUsers) {
                this[UserTable.name] = it.name
                this[UserTable.email] = it.email
            }.map {
                User(
                    id = it[UserTable.id],
                    name = it[UserTable.name],
                    email = it[UserTable.email],
                )
            }
        }
    }

    override fun addMessages(newMessages: List<NewMessage>): List<Message> {
        return transaction {
            val msgResults = MessageTable.batchInsert(newMessages) {
                this[MessageTable.msg] = it.msg
                this[MessageTable.createdBy] = it.createdBy
            }
            val users = UserTable.select { UserTable.id inList msgResults.map { it[MessageTable.id] } }.map {
                it[UserTable.id] to User(
                    id = it[UserTable.id],
                    name = it[UserTable.name],
                    email = it[UserTable.email],
                )
            }.toMap()
            msgResults.map {
                Message(
                    id = it[MessageTable.id],
                    msg = it[MessageTable.msg],
                    createdBy = users[it[MessageTable.id]]!!,
                )
            }
        }
    }

    override fun getUser(id: Int): User {
        val row = transaction {
            UserTable.select { UserTable.id eq id }.first()
        }
        return User(
            id = row[UserTable.id],
            name = row[UserTable.name],
            email = row[UserTable.email],
        )
    }
}