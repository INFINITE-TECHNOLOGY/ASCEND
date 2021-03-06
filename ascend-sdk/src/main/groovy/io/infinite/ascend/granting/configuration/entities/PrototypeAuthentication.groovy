package io.infinite.ascend.granting.configuration.entities

import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class PrototypeAuthentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String name

}
