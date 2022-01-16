import data.exposed.dsl.ExposedDslGateway
import data.hibernate.core.CoreHibernateGateway
import data.jdbc.JdbcGateway
import domain.entities.NewMessage
import domain.entities.NewUser
import domain.gateways.IDatabaseGateway
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    println("Start")
    val url = args[0]
    println("--------")
    println("JDBC")
    run(JdbcGateway(url))
    println("--------")
    println("Exposed DSL")
    run(ExposedDslGateway(url))
    println("--------")
    println("Hibernate Core")
    run(CoreHibernateGateway(url))
    println("--------")
    println("Finish")
}

fun run(db: IDatabaseGateway) {
    println("Connect")
    db.connect()
    println("Create database")
    db.createDatabase()
    println("Create users")
    val newUsers = generateUsers(10)
    var time = measureTimeMillis {
        (1..100).forEach { _ ->
            val users = db.addUsers(newUsers)
        }
    }
    println("Time: %.4fms".format(time / 100f))
    println("Select users")
    time = measureTimeMillis {
        (1..1000).forEach {
            val user = db.getUser(it)
        }
    }
    println("Time: %.4fms".format(time / 1000f))
    println("Create messages")
    val newMessages = generateMessages(10, 1)
    time = measureTimeMillis {
        (1..100).forEach { _ ->
            val messages = db.addMessages(newMessages)
        }
    }
    println("Time: %.4fms".format(time / 100f))
    println("Drop database")
    db.dropDatabase()
    println("Disconnect")
    db.disconnect()
}

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun generateUsers(size: Int): List<NewUser> {
    return List(size) {
        NewUser(
            name = getRandomString(128),
            email = getRandomString(320),
        )
    }
}

fun generateMessages(size: Int, createdBy: Int): List<NewMessage> {
    return List(size) {
        NewMessage(
            msg = getRandomString(128),
            createdBy = createdBy,
        )
    }
}