package io.infinite.ascend.repositories


import io.infinite.ascend.entities.Usage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface UsageRepository extends JpaRepository<Usage, Long> {

    Set<Usage> findByAuthorizationId(@Param("authorizationId") UUID authorizationId)

}
