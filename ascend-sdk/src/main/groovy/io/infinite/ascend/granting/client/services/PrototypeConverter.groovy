package io.infinite.ascend.granting.client.services

import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Grant
import io.infinite.ascend.common.entities.Identity
import io.infinite.ascend.common.entities.Scope
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthentication
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import io.infinite.ascend.granting.configuration.entities.PrototypeGrant
import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity
import io.infinite.ascend.granting.configuration.entities.PrototypeScope
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

    Scope convertScope(PrototypeScope prototypeScope) {
        return new Scope(
                name: prototypeScope.name
        )
    }

}
