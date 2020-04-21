package io.infinite.ascend.granting.configuration.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString
import io.infinite.ascend.common.entities.Authorization

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.OrderColumn

@Entity
@ToString(includeNames = true, includeFields = true)
class PrototypeNamespace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    String name
    
    @OneToMany
    Set<PrototypeAuthorization> authorizations

}
