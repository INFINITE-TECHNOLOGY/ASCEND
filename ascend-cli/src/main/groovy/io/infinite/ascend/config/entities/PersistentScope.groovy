package io.infinite.ascend.config.entities

import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class PersistentScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String name

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<PersistentGrant> grantTypes = new HashSet<PersistentGrant>()

}
