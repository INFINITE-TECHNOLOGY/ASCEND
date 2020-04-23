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

    Authorization convertAuthorization(PrototypeAuthorization prototypeAuthorization, String authorizationClientNamespace, String authorizationServerNamespace) {
        return new Authorization(
                name: prototypeAuthorization.name,
                serverNamespace: authorizationServerNamespace,
                isRefresh: prototypeAuthorization.isRefresh,
                clientNamespace: authorizationClientNamespace
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

    Grant convertGrant(PrototypeGrant prototypeGrant) {
        return new Grant(
                urlRegex: prototypeGrant.urlRegex,
                bodyRegex: prototypeGrant.bodyRegex,
                httpMethod: prototypeGrant.httpMethod
        )
    }
}
