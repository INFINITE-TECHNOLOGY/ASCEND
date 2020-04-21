package io.infinite.ascend.granting.configuration.repositories

import io.infinite.ascend.granting.configuration.entities.PrototypeNamespace
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PrototypeNamespaceRepository extends JpaRepository<PrototypeNamespace, Long> {

    Optional<PrototypeNamespace> findByName(String name)

}
