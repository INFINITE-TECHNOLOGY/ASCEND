package io.infinite.ascend.granting.server.repositories

import io.infinite.ascend.granting.server.entities.TrustedApp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface TrustedAppRepository extends JpaRepository<TrustedApp, Long> {

    Optional<TrustedApp> findByAppName(
            @Param("appName") String appName
    )

}