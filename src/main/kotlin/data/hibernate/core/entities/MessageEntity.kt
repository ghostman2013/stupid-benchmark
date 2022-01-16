package data.hibernate.core.entities

import javax.persistence.*

@Table(name = "messages")
@Entity
open class MessageEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    open val id: Int? = null,
    @Column(name="msg", nullable = false, length = 1024)
    open val msg: String = "",
    @ManyToOne(targetEntity = UserEntity::class, fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    open val createdBy: UserEntity = UserEntity()
)
