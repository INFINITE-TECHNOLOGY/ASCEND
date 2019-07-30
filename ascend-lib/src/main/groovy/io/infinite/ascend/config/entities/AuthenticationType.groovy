package io.infinite.ascend.config.entities

import javax.persistence.*

@Entity
class AuthenticationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String name

}
