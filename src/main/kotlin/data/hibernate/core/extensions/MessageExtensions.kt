package data.hibernate.core.extensions

import data.hibernate.core.entities.MessageEntity
import data.hibernate.core.entities.UserEntity
import domain.entities.NewMessage

fun NewMessage.toMessageEntity(creator: UserEntity) = MessageEntity(
    msg = this.msg,
    createdBy = creator
)
