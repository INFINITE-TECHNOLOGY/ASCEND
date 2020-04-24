package io.infinite.ascend.granting.client.services

import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import org.springframework.stereotype.Service

@Service
class DefaultPrototypeAuthorizationSelector implements PrototypeAuthorizationSelector{

    @Override
    PrototypeAuthorization select(Set<PrototypeAuthorization> prototypeAuthorizations) {
        return prototypeAuthorizations.first()
    }

    @Override
    PrototypeAuthorization selectPrerequisite(Set<PrototypeAuthorization> prototypeAuthorizations) {
        return prototypeAuthorizations.first()
    }

}
