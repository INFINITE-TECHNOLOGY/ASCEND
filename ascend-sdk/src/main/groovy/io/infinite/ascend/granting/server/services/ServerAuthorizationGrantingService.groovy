package io.infinite.ascend.granting.server.services


import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Grant
import io.infinite.ascend.common.entities.Scope
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthentication
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import io.infinite.ascend.granting.configuration.repositories.PrototypeAuthorizationRepository
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.time.Duration
import java.time.Instant

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class ServerAuthorizationGrantingService {

    @Autowired
    PrototypeAuthorizationRepository authorizationTypeRepository

    @Autowired
    JwtService jwtService

    @Autowired
    ServerAuthenticationService serverAuthenticationService

    @Value("jwtAccessKeyPublic")
    String jwtAccessKeyPublic

    @Value("jwtAccessKeyPrivate")
    String jwtAccessKeyPrivate

    Authorization authorize(Authorization authorization) {
        try {
            Set<PrototypeAuthorization> authorizationTypes = authorizationTypeRepository.findForGranting(
                    authorization.namespace,
                    authorization.name,
                    authorization.scope?.name,
                    authorization.identity?.name
            )
            if (authorizationTypes.size() == 0) {
                log.debug("No authorization types found")
                failure(authorization)
                return authorization
            }
            PrototypeAuthorization authorizationType = authorizationTypes.first()
            grantByType(authorization, authorizationType)
            return authorization
        } catch (Exception e) {
            log.warn("Exception during granting", e)
            failure(authorization)
            return authorization
        } finally {
            authorization.identity?.authentications?.each { Authentication authentication ->
                authentication.authenticationData?.privateCredentials = null
            }
        }
    }

    void grantByType(Authorization authorization, PrototypeAuthorization prototypeAuthorization) {
        Map<String, String> prerequisiteAuthenticatedCredentials
        if (prototypeAuthorization.prerequisites.size() != 0) {
            if (authorization.prerequisiteJwt == null) {
                log.debug("Missing prerequisite")
                failure(authorization)
                return
            }
            Authorization prerequisiteAuthorization = jwtService.jwt2Authorization(authorization.prerequisiteJwt, jwtService.loadPublicKeyFromHexString(jwtAccessKeyPublic))
            Boolean prerequisiteFound = false
            prerequisiteAuthenticatedCredentials = prerequisiteAuthorization?.identity?.authenticatedCredentials
            for (PrototypeAuthorization prerequisiteAuthorizationType in prototypeAuthorization.prerequisites) {
                if (prerequisiteAuthorizationType.name == prerequisiteAuthorization.name) {
                    grantByType(prerequisiteAuthorization, prerequisiteAuthorizationType)
                    if (!prerequisiteAuthorization.isSuccessful) {
                        log.debug("Failed prerequisite")
                        failure(authorization)
                        return
                    }
                    if (prerequisiteAuthorization.expiryDate.before(new Date())) {
                        log.debug("Expired prerequisite")
                        failure(authorization)
                        return
                    }
                    prerequisiteFound = true
                    break
                }
            }
            if (!prerequisiteFound) {
                log.debug("Wrong prerequisite type")
                failure(authorization)
                return
            }
        }
        for (PrototypeAuthentication authenticationType in prototypeAuthorization.identities.first().authentications) {
            Boolean authenticationFound = false
            for (Authentication authentication in authorization.identity.authentications) {
                if (authentication.name == authenticationType.name) {
                    authenticationFound = true
                    Map<String, String> authenticatedCredentials = commonAuthenticationValidation(authentication, authorization)
                    if (!authentication.isSuccessful) {
                        log.debug("Failed authentication")
                        failureAuthentication(authorization)
                        return
                    } else {
                        if (authenticatedCredentials != null) {
                            for (authenticatedCredentialsName in authenticatedCredentials.keySet()) {
                                if (authorization.identity.authenticatedCredentials.containsKey(authenticatedCredentialsName)) {
                                    if (authorization.identity.authenticatedCredentials.get(authenticatedCredentialsName) !=
                                            authenticatedCredentials.get(authenticatedCredentialsName)) {
                                        log.debug("Inconsistent authenticated credentials")
                                        failure(authorization)
                                        return
                                    }
                                } else {
                                    authorization.identity.authenticatedCredentials.put(authenticatedCredentialsName, authenticatedCredentials.get(authenticatedCredentialsName))
                                }
                            }
                        }
                    }
                    break
                }
            }
            if (!authenticationFound) {
                log.debug("Missing authentication")
                failure(authorization)
                return
            }
        }
        if (prerequisiteAuthenticatedCredentials != null) {
            authorization.identity.authenticatedCredentials.each {
                if (prerequisiteAuthenticatedCredentials.get(it.key) != it.value) {
                    log.debug("Inconsistent prerequisite")
                    failure(authorization)
                    return
                }
            }
        }
        log.debug("Success")
        authorization.durationSeconds = prototypeAuthorization.durationSeconds
        authorization.maxUsageCount = prototypeAuthorization.maxUsageCount
        authorization.scope = new Scope(
                grants: prototypeAuthorization.scopes.first().grants.collect {
                    new Grant(
                            urlRegex: it.urlRegex,
                            httpMethod: it.httpMethod
                    )
                }.toSet()
        )
        authorization.isRefresh = false
        authorization.isSuccessful = true
        authorization.guid = UUID.randomUUID()
        log.debug(authorization.id.toString())
        Instant creationDate = Instant.now()
        authorization.creationDate = creationDate.toDate()
        authorization.expiryDate = (creationDate + Duration.ofSeconds(prototypeAuthorization.durationSeconds)).toDate()
        jwtService.authorization2Jwt(authorization, jwtService.loadPrivateKeyFromHexString(jwtAccessKeyPrivate))
    }

    Map<String, String> commonAuthenticationValidation(Authentication authentication, Authorization authorization) {
        Map<String, String> authenticatedCredentials = serverAuthenticationService.authenticate(authentication, authorization)
        authentication.authenticationData?.privateCredentials = null
        return authenticatedCredentials
    }

    void failure(Authorization authorization) {
        authorization.isSuccessful = false
    }

    void failureAuthentication(Authorization authorization) {
        authorization.isSuccessful = false
        authorization.isAuthenticationFailed = true
    }

}
