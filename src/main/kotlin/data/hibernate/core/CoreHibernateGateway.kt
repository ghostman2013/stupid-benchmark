package data.hibernate.core

import data.hibernate.core.entities.MessageEntity
import data.hibernate.core.entities.UserEntity
import data.hibernate.core.extensions.toMessageEntity
import data.hibernate.core.extensions.toUser
import data.hibernate.core.extensions.toUserEntity
import domain.entities.Message
import domain.entities.NewMessage
import domain.entities.NewUser
import domain.entities.User
import domain.gateways.IDatabaseGateway
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration


class CoreHibernateGateway(private val url: String): IDatabaseGateway {
    private lateinit var sessionFactory: SessionFactory
    private lateinit var session: Session

    override fun connect() {
        val configuration = Configuration()
            .addAnnotatedClass(UserEntity::class.java)
            .addAnnotatedClass(MessageEntity::class.java)
            .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
            .setProperty("hibernate.connection.url", url)
            .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
            .setProperty("hibernate.connection.pool_size", "0")
        this.sessionFactory = configuration.buildSessionFactory()
        this.session = this.sessionFactory.openSession()
    }

    override fun disconnect() {
        this.session.close()
        this.sessionFactory.close()
    }

    override fun createDatabase() {
        val tx = this.session.beginTransaction()
        val query = this.session.createNativeQuery("""
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
        """)
        query.executeUpdate()
        tx.commit()
    }

    override fun dropDatabase() {
        val tx = this.session.beginTransaction()
        val query = this.session.createNativeQuery("""
            DROP TABLE messages;
            DROP TABLE users;
        """)
        query.executeUpdate()
        tx.commit()
    }

    override fun addUsers(newUsers: List<NewUser>): List<User> {
        val tx = this.session.beginTransaction()
        val users = newUsers.map {
            val id = this.session.save(it.toUserEntity()) as Int
            User(
                id = id,
                name = it.name,
                email = it.email
            )
        }
        tx.commit()
        return users
    }

    override fun addMessages(newMessages: List<NewMessage>): List<Message> {
        val tx = this.session.beginTransaction()
        val map = hashMapOf<Int, ArrayList<NewMessage>>()
        newMessages.forEach {
            val values = map[it.createdBy]
            if (values != null) {
                values.add(it)
            } else {
                map[it.createdBy] = arrayListOf(it)
            }
        }
        val messages = ArrayList<Message>(newMessages.size)
        map.map {
            val userEntity = this.session.get(UserEntity::class.java, it.key)
            val user = userEntity.toUser()
            it.value.forEach { newMessage ->
                val id = this.session.save(newMessage.toMessageEntity(userEntity)) as Int
                messages.add(Message(
                    id = id,
                    msg = newMessage.msg,
                    createdBy = user,
                ))
            }
        }
        tx.commit()
        return messages
    }

    override fun getUser(id: Int): User {
        val tx = this.session.beginTransaction()
        val user = this.session.get(UserEntity::class.java, id)
        tx.commit()
        return user.toUser()
    }
}