package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.IdentityType
import org.springframework.data.jpa.repository.JpaRepository

interface IdentityTypeRepository extends JpaRepository<IdentityType, Long> {


}
