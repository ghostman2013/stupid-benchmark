package data.hibernate.core.entities

import javax.persistence.*

@Table(name = "users")
@Entity
open class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    open val id: Int? = null,
    @Column(name="name", nullable = false, length = 128)
    open val name: String = "",
    @Column(name="email", nullable = false, length = 320)
    open val email: String = "",
    @OneToMany(
        targetEntity = MessageEntity::class,
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    open val messages: List<MessageEntity> = mutableListOf()
)
