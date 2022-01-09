package data.sql

import domain.entities.Message
import domain.entities.NewMessage
import domain.entities.NewUser
import domain.entities.User
import domain.gateways.IDatabaseGateway
import java.sql.Connection
import java.sql.DriverManager

class SqlDatabaseGateway(private val url: String): IDatabaseGateway {
    private lateinit var connection: Connection

    override fun connect() {
       this.connection = DriverManager.getConnection(this.url)
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
        val sql = "INSERT INTO users(name, email) VALUES $args RETURNING id, name, email"
        val statement = this.connection.prepareStatement(sql)
        var index = 1
        newUsers.forEach {
            statement.setString(index++, it.name)
            statement.setString(index++, it.email)
        }
        val rs = statement.executeQuery()
        val users = ArrayList<User>(newUsers.size)
        while (rs.next()) {
            users.add(User(
                id = rs.getInt(1),
                name = rs.getString(2),
                email = rs.getString(3),
            ))
        }
        rs.close()
        statement.close()
        return users
    }

    override fun addMessages(newMessages: List<NewMessage>): List<Message> {
        val args = List(newMessages.size) { "(?,?)" }.joinToString(",")
        val sql = """
            WITH msg_list AS (
                INSERT INTO messages(msg, created_by) VALUES $args RETURNING id, msg, created_by
            )
            SELECT m.id, m.msg, m.created_by, u.id, u.name, u.email
            FROM msg_list m
            INNER JOIN users u on u.id = m.created_by
            """
        val statement = this.connection.prepareStatement(sql)
        var index = 1
        newMessages.forEach {
            statement.setString(index++, it.msg)
            statement.setInt(index++, it.createdBy)
        }
        val rs = statement.executeQuery()
        val messages = ArrayList<Message>(newMessages.size)
        while (rs.next()) {
            messages.add(Message(
                id = rs.getInt(1),
                msg = rs.getString(2),
                createdBy = User(
                    id = rs.getInt(3),
                    name = rs.getString(4),
                    email = rs.getString(5),
                ),
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