package io.infinite.ascend.config.entities

import io.infinite.ascend.config.entities.enums.HttpMethod

import javax.persistence.*

@Entity
class Grant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String validationPluginName

    String urlMask

    HttpMethod httpMethod

}
