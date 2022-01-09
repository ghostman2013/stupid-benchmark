package data.dsl.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object MessageTable: Table("messages") {
    val id = integer("id").autoIncrement()
    val msg = varchar("msg", 1024)
    val createdBy = integer("created_by")
        .references(UserTable.id, onUpdate = ReferenceOption.CASCADE, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(id)
}