package io.infinite.ascend.common.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class Identity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    String name

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable//todo: remove join table?
    Set<Authentication> authentications = new HashSet<Authentication>()

    @ElementCollection(fetch = FetchType.EAGER)
    Map<String, String> publicCredentials = new HashMap<String, String>()

}
