package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.AuthorizationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface AuthorizationTypeRepository extends JpaRepository<AuthorizationType, Long> {

    @Query("""select a from AuthorizationType a
        join a.scopeSet scopeSet
        where scopeSet.name = :scopeName""")
    Set<AuthorizationType> findByScopeName(
            @Param("scopeName") String scopeName
    )

    @Query("""select a from AuthorizationType a
        join a.identitySet identitySet
        join a.scopeSet scopeSet
        where a.name = :authorizationName
        and scopeSet.name = :scopeName
        and identitySet.name = :identityName """)
    Set<AuthorizationType> findForGranting(
            @Param("authorizationName") String authorizationName
            , @Param("scopeName") String scopeName
            , @Param("identityName") String identityName
    )

}