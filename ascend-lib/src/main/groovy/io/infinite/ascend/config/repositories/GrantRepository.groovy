package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.Grant
import org.springframework.data.jpa.repository.JpaRepository

interface GrantRepository extends JpaRepository<Grant, Long> {


}
