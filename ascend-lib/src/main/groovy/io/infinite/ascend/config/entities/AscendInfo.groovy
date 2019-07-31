package io.infinite.ascend.config.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class AscendInfo {

    @Id
    @Column(nullable = false)
    final Long id = 1

    Boolean isDataInitialized

}
