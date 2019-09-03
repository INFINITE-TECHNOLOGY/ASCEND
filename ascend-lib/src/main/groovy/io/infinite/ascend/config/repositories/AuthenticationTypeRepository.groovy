package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.AuthenticationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface AuthenticationTypeRepository extends JpaRepository<AuthenticationType, Long> {


}
