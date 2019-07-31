package io.infinite.ascend.config.entities

import groovy.transform.ToString
import io.infinite.ascend.config.entities.enums.HttpMethod

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class Grant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String bodyRegex

    String urlRegex

    HttpMethod httpMethod

}
