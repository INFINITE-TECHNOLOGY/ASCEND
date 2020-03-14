package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.PersistentAuthorization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PersistentAuthorizationRepository extends JpaRepository<PersistentAuthorization, Long> {

    @Query("""select a from AuthorizationType a
        join a.scopes scopes
        where scopes.name = :scopeName""")
    Set<PersistentAuthorization> findByScopeName(
            @Param("scopeName") String scopeName
    )

    @Query("""select a from AuthorizationType a
        join a.identityTypes identityTypes
        join a.scopes scopes
        where a.name = :authorizationName
        and scopes.name = :scopeName
        and identityTypes.name = :identityTypeName""")
    Set<PersistentAuthorization> findForGranting(
            @Param("authorizationName") String authorizationName
            , @Param("scopeName") String scopeName
            , @Param("identityTypeName") String identityTypeName
    )

}