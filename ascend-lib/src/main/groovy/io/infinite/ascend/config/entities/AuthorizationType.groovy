package io.infinite.ascend.config.entities

import groovy.transform.ToString

import javax.persistence.*

@Entity
@ToString(includeNames = true, includeFields = true)
class AuthorizationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String name

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<IdentityType> identityTypes = new HashSet<IdentityType>()

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<Scope> scopes = new HashSet<Scope>()

    Integer durationSeconds

    Integer maxUsageCount

    Integer refreshDurationSeconds

    Integer refreshMaxUsageCount

    Boolean isRefreshAllowed

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<AuthorizationType> prerequisiteAuthorizationTypes = new HashSet<AuthorizationType>()

}
