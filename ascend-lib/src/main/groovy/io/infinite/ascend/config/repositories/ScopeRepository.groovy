package io.infinite.ascend.config.repositories

import io.infinite.ascend.config.entities.Scope
import org.springframework.data.jpa.repository.JpaRepository

interface ScopeRepository extends JpaRepository<Scope, Long> {


}
