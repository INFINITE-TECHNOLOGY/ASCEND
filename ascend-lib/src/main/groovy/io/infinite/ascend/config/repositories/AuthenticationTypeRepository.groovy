package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.AuthenticationType
import org.springframework.data.jpa.repository.JpaRepository

interface AuthenticationTypeRepository extends JpaRepository<AuthenticationType, Long> {


}
