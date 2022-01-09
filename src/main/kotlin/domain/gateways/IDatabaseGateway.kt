package domain.gateways

import domain.entities.Message
import domain.entities.NewMessage
import domain.entities.NewUser
import domain.entities.User

interface IDatabaseGateway {
    fun connect()
    fun disconnect()
    fun createDatabase()
    fun dropDatabase()
    fun addUsers(newUsers: List<NewUser>): List<User>
    fun addMessages(newMessages: List<NewMessage>): List<Message>
    fun getUser(id: Int): User
}