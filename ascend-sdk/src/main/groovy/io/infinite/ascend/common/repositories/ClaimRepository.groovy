package io.infinite.ascend.common.repositories


import io.infinite.ascend.common.entities.Claim
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface ClaimRepository extends JpaRepository<Claim, Long> {

}
