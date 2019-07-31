package io.infinite.ascend.config.entities

import groovy.transform.ToString

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@ToString(includeNames = true, includeFields = true)
class AscendInstance {

    @Id
    @Column(nullable = false)
    final Long id = 1

    Boolean isDataInitialized

}
