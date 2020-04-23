package io.infinite.ascend.granting.configuration.repositories

import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PrototypeAuthorizationRepository extends JpaRepository<PrototypeAuthorization, Long> {

    @Query("""select a from PrototypeAuthorization a
        join a.scopes s
        where s.name = :scopeName
        and a.serverNamespace = :serverNamespace""")
    Set<PrototypeAuthorization> inquire(
            @Param("scopeName") String scopeName,
            @Param("serverNamespace") String serverNamespace
    )

    @Query("""select a from PrototypeAuthorization a
        join a.identities i
        join a.scopes s
        where a.name = :authorizationName
        and s.name = :scopeName
        and i.name = :identityTypeName
        and a.serverNamespace = :serverNamespace
        and a.isRefresh = :isRefresh""")
    Set<PrototypeAuthorization> findForGranting(
            @Param("serverNamespace") String serverNamespace
            , @Param("authorizationName") String authorizationName
            , @Param("scopeName") String scopeName
            , @Param("identityTypeName") String identityTypeName
            , @Param("isRefresh") Boolean isRefresh
    )

}