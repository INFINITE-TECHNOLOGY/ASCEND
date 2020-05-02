package io.infinite.ascend.common.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class Scope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    Long id

    String name

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable
    Set<Grant> grants = new HashSet<Grant>()

}
