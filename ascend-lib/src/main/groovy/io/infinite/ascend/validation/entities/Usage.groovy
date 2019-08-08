package io.infinite.ascend.validation.entities

import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class Usage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long authorizationUsageId

    @Column(nullable = false)
    UUID authorizationId

    Date usageDate

}
