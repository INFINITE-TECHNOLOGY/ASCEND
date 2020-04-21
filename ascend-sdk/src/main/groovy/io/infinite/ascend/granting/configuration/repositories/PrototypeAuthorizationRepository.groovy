package io.infinite.ascend.granting.configuration.repositories

import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PrototypeAuthorizationRepository extends JpaRepository<PrototypeAuthorization, Long> {

    @Query("""select a from PrototypeNamespace n 
        join n.authorizations a
        join a.scopes scopes
        where scopes.name = :scopeName
        and n.name = :namespace""")
    Set<PrototypeAuthorization> inquire(
            @Param("scopeName") String scopeName,
            @Param("namespace") String namespace
    )

    @Query("""select a from  PrototypeNamespace n
        join n.authorizations a
        join a.identities identityTypes
        join a.scopes scopes
        where a.name = :authorizationName
        and scopes.name = :scopeName
        and identityTypes.name = :identityTypeName
        and n.name = :authorizationNamespace""")
    Set<PrototypeAuthorization> findForGranting(
            @Param("authorizationNamespace") String authorizationNamespace
            , @Param("authorizationName") String authorizationName
            , @Param("scopeName") String scopeName
            , @Param("identityTypeName") String identityTypeName
    )

}