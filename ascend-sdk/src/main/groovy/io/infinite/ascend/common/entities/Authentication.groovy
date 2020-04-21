package io.infinite.ascend.common.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class Authentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    String name

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    AuthenticationData authenticationData

    Boolean isSuccessful

}
