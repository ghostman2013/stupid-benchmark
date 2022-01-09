package data.dsl.tables

import org.jetbrains.exposed.sql.Table

object UserTable: Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 128)
    val email = varchar("email", 320)
    override val primaryKey = PrimaryKey(id)
}
