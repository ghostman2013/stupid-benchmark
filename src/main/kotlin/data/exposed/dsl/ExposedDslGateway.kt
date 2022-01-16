package data.exposed.dsl

import data.exposed.dsl.tables.MessageTable
import data.exposed.dsl.tables.UserTable
import domain.entities.Message
import domain.entities.NewMessage
import domain.entities.NewUser
import domain.entities.User
import domain.gateways.IDatabaseGateway
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transactionManager

class ExposedDslGateway(private var url: String): IDatabaseGateway {
    private lateinit var db: Database
    private lateinit var transaction: Transaction

    override fun connect() {
        this.db = Database.connect(url)
        this.transaction = this.db.transactionManager.newTransaction()
    }

    override fun disconnect() {
        this.transaction.close()
        TransactionManager.closeAndUnregister(this.db)
    }

    override fun createDatabase() {
        SchemaUtils.create(UserTable, MessageTable)
        this.transaction.commit()
    }

    override fun dropDatabase() {
        SchemaUtils.drop(UserTable, MessageTable)
        this.transaction.commit()
    }

    override fun addUsers(newUsers: List<NewUser>): List<User> {
        val users = UserTable.batchInsert(newUsers) {
            this[UserTable.name] = it.name
            this[UserTable.email] = it.email
        }.map {
            User(
                id = it[UserTable.id],
                name = it[UserTable.name],
                email = it[UserTable.email],
            )
        }
        this.transaction.commit()
        return users
    }

    override fun addMessages(newMessages: List<NewMessage>): List<Message> {
        val msgResults = MessageTable.batchInsert(newMessages) {
            this[MessageTable.msg] = it.msg
            this[MessageTable.createdBy] = it.createdBy
        }
        val users = UserTable.select { UserTable.id inList msgResults.map { it[MessageTable.id] } }.associate {
            it[UserTable.id] to User(
                id = it[UserTable.id],
                name = it[UserTable.name],
                email = it[UserTable.email],
            )
        }
        val messages = msgResults.map {
            Message(
                id = it[MessageTable.id],
                msg = it[MessageTable.msg],
                createdBy = users[it[MessageTable.id]]!!,
            )
        }
        this.transaction.commit()
        return messages
    }

    override fun getUser(id: Int): User {
        val row = UserTable.select { UserTable.id eq id }.first()
        this.transaction.commit()
        return User(
            id = row[UserTable.id],
            name = row[UserTable.name],
            email = row[UserTable.email],
        )
    }
}