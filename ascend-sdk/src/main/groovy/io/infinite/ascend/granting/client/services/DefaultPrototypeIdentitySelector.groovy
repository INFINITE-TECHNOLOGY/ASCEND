package io.infinite.ascend.granting.client.services

import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity
import org.springframework.stereotype.Service

@Service
class DefaultPrototypeIdentitySelector implements PrototypeIdentitySelector {

    @Override
    PrototypeIdentity select(Set<PrototypeIdentity> identities) {
        return identities.first()
    }

    @Override
    PrototypeIdentity selectPrerequisite(Set<PrototypeIdentity> identities) {
        return identities.first()
    }

}
