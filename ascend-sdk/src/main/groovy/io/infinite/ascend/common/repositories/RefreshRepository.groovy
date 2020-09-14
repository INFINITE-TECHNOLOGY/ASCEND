package io.infinite.ascend.common.repositories


import io.infinite.ascend.common.entities.Refresh
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface RefreshRepository extends JpaRepository<Refresh, Long> {

    Optional<Refresh> findByGuid(UUID guid)

    @Query("""select r from Refresh r
        where r.authorizationName = :authorizationName
        and r.serverNamespace = :serverNamespace
        and r.clientNamespace = :clientNamespace
        and r.expiryDate > :cutoffDate""")
    Set<Refresh> findRefresh(
            @Param("clientNamespace") String clientNamespace,
            @Param("serverNamespace") String serverNamespace,
            @Param("authorizationName") String authorizationName,
            @Param("cutoffDate") Date cutoffDate
    )

    @Query("""select r from Refresh r
        where r.clientNamespace = :clientNamespace""")
    Set<Refresh> findByClientNamespace(String clientNamespace)

}
