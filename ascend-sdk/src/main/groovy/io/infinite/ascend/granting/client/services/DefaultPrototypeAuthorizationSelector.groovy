package io.infinite.ascend.granting.client.services

import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class DefaultPrototypeAuthorizationSelector implements PrototypeAuthorizationSelector{

    @Override
    PrototypeAuthorization select(Set<PrototypeAuthorization> prototypeAuthorizations) {
        return prototypeAuthorizations.first()
    }

}
