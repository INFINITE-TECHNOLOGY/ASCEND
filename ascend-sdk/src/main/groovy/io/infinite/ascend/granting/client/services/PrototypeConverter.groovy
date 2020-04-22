package io.infinite.ascend.granting.client.services

import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Identity
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthentication
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity
import org.springframework.stereotype.Service

@Service
class PrototypeConverter {

    Authorization convertAuthorization(PrototypeAuthorization prototypeAuthorization, String authorizationNamespace) {
        return new Authorization(
                name: prototypeAuthorization.name,
                namespace: authorizationNamespace,
                isRefresh: prototypeAuthorization.isRefresh
        )
    }

    Identity convertIdentity(PrototypeIdentity prototypeIdentity) {
        return new Identity(
                name: prototypeIdentity.name
        )
    }

    Authentication convertAuthentication(PrototypeAuthentication prototypeAuthentication) {
        return new Authentication(
                name: prototypeAuthentication.name
        )
    }

}
