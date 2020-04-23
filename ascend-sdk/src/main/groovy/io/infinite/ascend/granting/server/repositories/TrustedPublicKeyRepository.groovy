package io.infinite.ascend.granting.server.repositories

import io.infinite.ascend.granting.server.entities.TrustedPublicKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface TrustedPublicKeyRepository extends JpaRepository<TrustedPublicKey, Long> {

    Optional<TrustedPublicKey> findByKeyName(String keyName)

}