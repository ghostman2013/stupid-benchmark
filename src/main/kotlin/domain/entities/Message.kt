package domain.entities

data class Message(
    val id: Int,
    val msg: String,
    val createdBy: User
)
