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

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<PrototypeIdentity> identities = new HashSet<PrototypeIdentity>()

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<PrototypeScope> scopes = new HashSet<PrototypeScope>()

    Integer durationSeconds

    Integer maxUsageCount

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<PrototypeAuthorization> prerequisites = new HashSet<PrototypeAuthorization>()

    @OneToOne(fetch = FetchType.EAGER)
    PrototypeAuthorization refresh

    @Column(nullable = false)
    Boolean isRefresh

}
