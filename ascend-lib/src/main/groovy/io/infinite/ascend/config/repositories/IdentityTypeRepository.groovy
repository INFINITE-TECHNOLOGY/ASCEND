package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.IdentityType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface IdentityTypeRepository extends JpaRepository<IdentityType, Long> {


}
