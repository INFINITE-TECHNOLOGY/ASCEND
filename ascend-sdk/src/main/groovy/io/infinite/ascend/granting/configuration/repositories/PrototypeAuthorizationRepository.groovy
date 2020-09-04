package io.infinite.ascend.granting.configuration.repositories


import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PrototypeAuthorizationRepository extends JpaRepository<PrototypeAuthorization, Long> {

    @Query("""select a from PrototypeAuthorization a
        join fetch a.scope s
        where s.name = :scopeName
        and a.serverNamespace = :serverNamespace""")
    Set<PrototypeAuthorization> inquire(
            @Param("scopeName") String scopeName,
            @Param("serverNamespace") String serverNamespace
    )

    @Query("""select a from PrototypeAuthorization a
        join fetch a.identities i
        join fetch a.scope s
        where a.name = :authorizationName
        and s.name = :scopeName
        and i.name = :identityName
        and a.serverNamespace = :serverNamespace""")
    Optional<PrototypeAuthorization> findForGranting(
            @Param("serverNamespace") String serverNamespace
            , @Param("authorizationName") String authorizationName
            , @Param("scopeName") String scopeName
            , @Param("identityName") String identityName
    )

    @Query("""select a from PrototypeAuthorization a
        join fetch a.identities i
        join fetch a.scope s
        where a.serverNamespace = :serverNamespace
        and a.name = :refreshAuthorizationName
        and i.name = :identityName
        and s.name = :scopeName""")
    Optional<PrototypeAuthorization> findAccessByRefresh(
            @Param("serverNamespace") String serverNamespace,
            @Param("refreshAuthorizationName") String refreshAuthorizationName,
            @Param("identityName") String identityName,
            @Param("scopeName") String scopeName
    )

}