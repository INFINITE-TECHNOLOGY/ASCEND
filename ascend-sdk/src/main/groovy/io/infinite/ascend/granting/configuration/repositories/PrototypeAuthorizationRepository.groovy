package io.infinite.ascend.granting.configuration.repositories

import io.infinite.ascend.common.entities.Refresh
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PrototypeAuthorizationRepository extends JpaRepository<PrototypeAuthorization, Long> {

    @Query("""select a from PrototypeAuthorization a
        join fetch a.scopes s
        where s.name = :scopeName
        and a.serverNamespace = :serverNamespace""")
    Set<PrototypeAuthorization> inquire(
            @Param("scopeName") String scopeName,
            @Param("serverNamespace") String serverNamespace
    )

    @Query("""select a from PrototypeAuthorization a
        join fetch a.identities i
        where a.name = :authorizationName
        and i.name = :identityName
        and a.serverNamespace = :serverNamespace""")
    Optional<PrototypeAuthorization> findForGranting(
            @Param("serverNamespace") String serverNamespace
            , @Param("authorizationName") String authorizationName
            , @Param("identityName") String identityName
    )

    @Query("""select a from PrototypeAuthorization a
        join fetch a.identities i
        where a.serverNamespace = :refresh.authorization.serverNamespace
        and a.name = :refresh.authorization.name
        and i.name = :refresh.identity.name""")
    Optional<PrototypeAuthorization> findAccessByRefresh(
            @Param("refresh") Refresh refresh
    )

}