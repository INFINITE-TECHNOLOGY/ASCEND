package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.PersistentGrant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PersistentGrantRepository extends JpaRepository<PersistentGrant, Long> {


}
