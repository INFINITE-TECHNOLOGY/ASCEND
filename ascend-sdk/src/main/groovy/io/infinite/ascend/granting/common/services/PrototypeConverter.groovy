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

    Set<Scope> convertScopes(Set<PrototypeScope> prototypeScopes) {
        return prototypeScopes.collect { prototypeScope ->
            new Scope(
                    name: prototypeScope.name,
                    grants: prototypeScope.grants.collect { prototypeGrant ->
                        convertGrant(prototypeGrant)
                    }.toSet()
            )
        }.toSet()
    }

    Scope convertLegacyScope(PrototypeScope prototypeScope) {
        return new Scope(
                name: prototypeScope.name,
                grants: prototypeScope.grants.collect { convertGrant(it) }.toSet()
        )
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    Grant convertGrant(PrototypeGrant prototypeGrant) {
        return new Grant(
                urlRegex: prototypeGrant.urlRegex,
                httpMethod: prototypeGrant.httpMethod
        )
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    Set<Scope> cloneScopes(Set<Scope> scopes) {
        List<Scope> clonedScopes = []
        scopes.each { scope ->
            clonedScopes.add(new Scope(
                    name: scope.name,
                    grants: scope.grants.collect { scopeGrant ->
                        new Grant(
                                urlRegex: scopeGrant.urlRegex,
                                httpMethod: scopeGrant.httpMethod
                        )
                    }.toSet()
            ))
        }
        return clonedScopes.toSet()
    }

    List<Scope> getScopesForLegacyRefresh(PrototypeAuthorization prototypeAuthorization) {
        List<Scope> scopes = []
        prototypeAuthorization.scopes.each {
            scopes += convertLegacyScope(it)
        }
        prototypeAuthorization.prerequisites.each {
            scopes += getScopesForLegacyRefresh(it)
        }
        return scopes
    }


}
