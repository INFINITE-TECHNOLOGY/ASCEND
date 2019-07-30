package io.infinite.ascend.config.entities


import javax.persistence.*

@Entity
class Scope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id

    String name

    @ManyToMany(fetch = FetchType.EAGER)
    @OrderColumn
    @JoinTable
    Set<Grant> grants = new HashSet<Grant>()

}
