package io.infinite.ascend.granting.configuration.repositories


import io.infinite.ascend.granting.configuration.entities.PrototypeRefresh
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface PrototypeRefreshRepository extends JpaRepository<PrototypeRefresh, Long> {


}
