package io.infinite.ascend.common.repositories

import io.infinite.ascend.common.entities.Refresh
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface RefreshRepository extends JpaRepository<Refresh, Long> {

    Optional<Refresh> findByGuid(UUID guid)

}
