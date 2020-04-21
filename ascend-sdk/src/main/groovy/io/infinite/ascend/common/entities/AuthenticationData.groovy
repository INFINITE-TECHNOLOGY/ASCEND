package io.infinite.ascend.common.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true, excludes = ["privateCredentials"])
class AuthenticationData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    @ElementCollection
    Map<String, String> publicCredentials = new HashMap<String, String>()

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    Map<String, String> privateCredentials = new HashMap<String, String>()

}
