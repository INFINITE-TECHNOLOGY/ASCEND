package io.infinite.ascend.config.entities

import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class PersistentAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String name

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<PersistentIdentity> identityTypes = new HashSet<PersistentIdentity>()

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<PersistentScope> scopes = new HashSet<PersistentScope>()

    Integer durationSeconds

    Integer maxUsageCount

    Integer refreshDurationSeconds

    Integer refreshMaxUsageCount

    Boolean isRefreshAllowed

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<PersistentAuthorization> prerequisiteAuthorizationTypes = new HashSet<PersistentAuthorization>()

}
