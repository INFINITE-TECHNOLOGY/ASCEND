package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.PersistentIdentity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PersistentIdentityRepository extends JpaRepository<PersistentIdentity, Long> {


}
