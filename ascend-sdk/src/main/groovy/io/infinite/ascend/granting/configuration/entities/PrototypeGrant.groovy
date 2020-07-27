package io.infinite.ascend.granting.configuration.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
@JsonIgnoreProperties(ignoreUnknown = true)
class PrototypeGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    String urlRegex

    String httpMethod

}
