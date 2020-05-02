package io.infinite.ascend.common.repositories

import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Refresh
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface AuthorizationRepository extends JpaRepository<Authorization, Long> {

    Optional<Authorization> findByGuid(UUID guid)

    @Query("""select a from Authorization a
        join a.scope s
        left join a.claims c
        where a.serverNamespace = :serverNamespace
        and a.clientNamespace = :clientNamespace
        and s.name = :scopeName
        and a.expiryDate > CURRENT_TIMESTAMP
        group by a.id, a.creationDate, a.maxUsageCount
        having (a.maxUsageCount > count(c) or a.maxUsageCount is null)
        order by a.creationDate""")
    Set<Authorization> findAuthorization(
            @Param("clientNamespace") String clientNamespace,
            @Param("serverNamespace") String serverNamespace,
            @Param("scopeName") String scopeName
    )

    @Query("""select a from Authorization a
        where a.serverNamespace = :serverNamespace
        and a.clientNamespace = :clientNamespace
        and a.name in :nameList
        and a.expiryDate > CURRENT_TIMESTAMP
        order by a.creationDate""")
    Set<Authorization> findPrerequisite(
            @Param("clientNamespace") String clientNamespace,
            @Param("serverNamespace") String serverNamespace,
            @Param("nameList") List<String> nameList
    )

    Set<Authorization> findByClientNamespace(String clientNamespace)

}
