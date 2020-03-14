package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.PersistentScope
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PersistentScopeRepository extends JpaRepository<PersistentScope, Long> {


}
