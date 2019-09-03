package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.AuthorizationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AuthorizationTypeRepository extends JpaRepository<AuthorizationType, Long> {

    @Query("""select a from AuthorizationType a
        join a.scopes scopes
        where scopes.name = :scopeName""")
    Set<AuthorizationType> findByScopeName(
            @Param("scopeName") String scopeName
    )

    @Query("""select a from AuthorizationType a
        join a.identityTypes identityTypes
        join a.scopes scopes
        where a.name = :authorizationName
        and scopes.name = :scopeName
        and identityTypes.name = :identityTypeName""")
    Set<AuthorizationType> findForGranting(
            @Param("authorizationName") String authorizationName
            , @Param("scopeName") String scopeName
            , @Param("identityTypeName") String identityTypeName
    )

}