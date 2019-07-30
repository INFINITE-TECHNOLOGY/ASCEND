package io.infinite.ascend.config.entities


import javax.persistence.*

@Entity
class IdentityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String name

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<AuthenticationType> authenticationTypes = new HashSet<AuthenticationType>()

}
