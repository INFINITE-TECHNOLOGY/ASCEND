package io.infinite.ascend.granting.configuration.entities

import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class PrototypeAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String name

    String serverNamespace

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<PrototypeIdentity> identities = new HashSet<PrototypeIdentity>()

    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @OrderColumn
    PrototypeScope scope

    Integer durationSeconds

    Integer maxUsageCount

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<PrototypeAuthorization> prerequisites = new HashSet<PrototypeAuthorization>()

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    PrototypeRefresh refresh

}
