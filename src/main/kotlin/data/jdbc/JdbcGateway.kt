package data.jdbc

import domain.entities.Message
import domain.entities.NewMessage
import domain.entities.NewUser
import domain.entities.User
import domain.gateways.IDatabaseGateway
import java.sql.Connection
import java.sql.DriverManager

class JdbcGateway(private val url: String): IDatabaseGateway {
    private lateinit var connection: Connection

    override fun connect() {
        this.connection = DriverManager.getConnection(url)
    }

    override fun disconnect() {
        this.connection.close()
    }

    override fun createDatabase() {
        val sql = """
            CREATE TABLE users (
                id int generated always as identity,
                name varchar(128) not null,
                email varchar(320) not null,
                primary key(id)
            );
            
            CREATE TABLE messages (
                id int generated always as identity,
                msg varchar(1024) not null,
                created_by int not null,
                primary key(id),
                foreign key(created_by) references users(id) on update cascade on delete cascade
            );
        """
        val statement = this.connection.createStatement()
        statement.execute(sql)
        statement.close()
    }

    override fun dropDatabase() {
        val sql = """
            DROP TABLE messages;
            DROP TABLE users;
        """
        val statement = this.connection.createStatement()
        statement.execute(sql)
        statement.close()
    }

    override fun addUsers(newUsers: List<NewUser>): List<User> {
        val args = List(newUsers.size) { "(?,?)" }.joinToString(",")
        val sql = "INSERT INTO users(name, email) VALUES $args RETURNING id"
        val statement = this.connection.prepareStatement(sql)
        var index = 1
        newUsers.forEach {
            statement.setString(index++, it.name)
            statement.setString(index++, it.email)
        }
        val rs = statement.executeQuery()
        val users = ArrayList<User>(newUsers.size)
        index = 0
        while (rs.next()) {
            val newUser = newUsers[index++]
            users.add(User(
                id = rs.getInt(1),
                name = newUser.name,
                email = newUser.email,
            ))
        }
        rs.close()
        statement.close()
        return users
    }

    override fun addMessages(newMessages: List<NewMessage>): List<Message> {
        val args = List(newMessages.size) { "(?,?)" }.joinToString(",")
        val sql = "INSERT INTO messages(msg, created_by) VALUES $args RETURNING id"
        val statement = this.connection.prepareStatement(sql)
        var index = 1
        newMessages.forEach {
            statement.setString(index++, it.msg)
            statement.setInt(index++, it.createdBy)
        }
        val rs = statement.executeQuery()
        val messages = ArrayList<Message>(newMessages.size)
        val creators = hashMapOf<Int, User>()
        index = 0
        while (rs.next()) {
            val newMessage = newMessages[index++]
            var creator = creators[newMessage.createdBy]
            if (creator == null) {
                val stmt = this.connection.prepareStatement("SELECT id, name, email FROM users WHERE id = ?")
                stmt.setInt(1, newMessage.createdBy)
                val userRs = stmt.executeQuery()
                userRs.next()
                creator = User(
                    id = userRs.getInt(1),
                    name = userRs.getString(2),
                    email = userRs.getString(3),
                )
                userRs.close()
                stmt.close()
                creators[creator.id] = creator
            }
            messages.add(Message(
                id = rs.getInt(1),
                msg = newMessage.msg,
                createdBy = creator,
            ))
        }
        rs.close()
        statement.close()
        return messages
    }

    override fun getUser(id: Int): User {
        val sql = "SELECT id, name, email FROM users WHERE id = ?"
        val statement = this.connection.prepareStatement(sql)
        statement.setInt(1, id)
        val rs = statement.executeQuery()
        rs.next()
        val user = User(
            id = rs.getInt(1),
            name = rs.getString(2),
            email = rs.getString(3),
        )
        rs.close()
        statement.close()
        return user
    }
}