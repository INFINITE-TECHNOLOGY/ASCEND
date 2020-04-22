package io.infinite.ascend.common.repositories

import io.infinite.ascend.common.entities.Authorization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

import javax.persistence.LockModeType

@RepositoryRestResource(exported = false)
interface AuthorizationRepository extends JpaRepository<Authorization, Long> {

    Optional<Authorization> findByGuid(UUID guid)

    @Query("""select a from Authorization a
        join a.scope s
        left join a.claims c
        where a.namespace = :namespace
        and a.name = :name
        and a.isSuccessful = true
        and a.expiryDate < CURRENT_DATE
        group by a.id, a.creationDate, a.maxUsageCount
        having (a.maxUsageCount > count(c) or a.maxUsageCount is null)
        order by a.creationDate""")
    Set<Authorization> findValidForClient(
            @Param("namespace") String namespace,
            @Param("name") String name
    )

}
