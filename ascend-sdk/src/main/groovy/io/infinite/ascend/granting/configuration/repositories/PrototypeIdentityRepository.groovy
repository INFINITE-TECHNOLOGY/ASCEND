package io.infinite.ascend.granting.configuration.repositories

import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PrototypeIdentityRepository extends JpaRepository<PrototypeIdentity, Long> {


}
