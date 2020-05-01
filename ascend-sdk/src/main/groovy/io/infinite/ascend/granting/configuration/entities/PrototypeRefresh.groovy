package io.infinite.ascend.granting.configuration.entities

import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class PrototypeRefresh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    Integer durationSeconds

    Integer maxUsageCount

    Boolean isRenewable

}
