package io.infinite.ascend.granting.configuration.entities

import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class PrototypeIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String name

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<PrototypeAuthentication> authentications = new HashSet<PrototypeAuthentication>()

}
