package io.infinite.ascend.granting.server.entities

import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class TrustedPublicKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String keyName

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    String publicKey

}
