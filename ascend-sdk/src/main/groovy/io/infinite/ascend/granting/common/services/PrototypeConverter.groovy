package io.infinite.ascend.granting.common.services

import io.infinite.ascend.common.entities.*
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.configuration.entities.*
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

import java.time.Duration
import java.time.Instant

@Service
@BlackBox(level = BlackBoxLevel.METHOD)
class PrototypeConverter {

    @Autowired
    JwtService jwtService

    @Autowired
    AuthorizationRepository authorizationRepository

    @Autowired
    ApplicationContext applicationContext

    Authorization convertAccessAuthorization(PrototypeAuthorization prototypeAuthorization, String clientNamespace) {
        return new Authorization(
                name: prototypeAuthorization.name,
                serverNamespace: prototypeAuthorization.serverNamespace,
                clientNamespace: clientNamespace,
                durationSeconds: prototypeAuthorization.durationSeconds,
                maxUsageCount: prototypeAuthorization.maxUsageCount,
                creationDate: Instant.now().toDate(),
                expiryDate: (Instant.now() + Duration.ofSeconds(prototypeAuthorization.durationSeconds)).toDate()
        )
    }

    Refresh convertRefresh(PrototypeAuthorization prototypeAuthorization, String clientNamespace) {
        return new Refresh(
                name: prototypeAuthorization.name,
                serverNamespace: prototypeAuthorization.serverNamespace,
                clientNamespace: clientNamespace,
                durationSeconds: prototypeAuthorization.refresh.durationSeconds,
                maxUsageCount: prototypeAuthorization.refresh.maxUsageCount,
                creationDate: Instant.now().toDate(),
                expiryDate: (Instant.now() + Duration.ofSeconds(prototypeAuthorization.refresh.durationSeconds)).toDate()
        )
    }

    Identity convertIdentity(PrototypeIdentity prototypeIdentity) {
        return new Identity(
                name: prototypeIdentity.name,
                authentications: prototypeIdentity.authentications.collect { prototypeAuthentication ->
                    convertAuthentication(prototypeAuthentication)
                }.toSet()
        )
    }

    Authentication convertAuthentication(PrototypeAuthentication prototypeAuthentication) {
        return new Authentication(
                name: prototypeAuthentication.name
        )
    }

    Scope convertScope(PrototypeScope prototypeScope) {
        return new Scope(
                name: prototypeScope.name,
                grants: prototypeScope.grants.collect { convertGrant(it) }.toSet()
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
