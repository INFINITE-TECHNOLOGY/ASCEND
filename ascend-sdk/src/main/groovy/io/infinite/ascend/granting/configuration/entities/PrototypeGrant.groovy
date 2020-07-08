package io.infinite.ascend.granting.configuration.entities

import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class PrototypeGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    String urlRegex

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    String bodyValidatorName

    String httpMethod

}
