package data.hibernate.core.extensions

import data.hibernate.core.entities.UserEntity
import domain.entities.NewUser
import domain.entities.User

fun NewUser.toUserEntity() = UserEntity(
    name = this.name,
    email = this.email
)

fun UserEntity.toUser() = User(
    id = this.id!!,
    name = this.name,
    email = this.email
)
