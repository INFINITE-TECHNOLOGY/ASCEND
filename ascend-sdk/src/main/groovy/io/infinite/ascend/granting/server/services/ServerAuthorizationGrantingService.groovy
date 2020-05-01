package io.infinite.ascend.granting.server.services

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Refresh
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.common.services.PrototypeConverter
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthentication
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import io.infinite.ascend.granting.configuration.repositories.PrototypeAuthorizationRepository
import io.infinite.ascend.common.exceptions.AscendForbiddenException
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class ServerAuthorizationGrantingService {

    @Autowired
    PrototypeAuthorizationRepository authorizationTypeRepository

    @Autowired
    JwtService jwtService

    @Autowired
    ServerAuthenticationValidationService serverAuthenticationService

    @Autowired
    AuthorizationRepository authorizationRepository

    @Autowired
    PrototypeConverter prototypeConverter

    @Autowired
    PrototypeAuthorizationRepository prototypeAuthorizationRepository

    Authorization grantAccessAuthorization(Authorization clientAuthorization) {
        try {
            Optional<PrototypeAuthorization> authorizationTypes = authorizationTypeRepository.findForGranting(
                    clientAuthorization.serverNamespace,
                    clientAuthorization.name,
                    clientAuthorization.scope?.name,
                    clientAuthorization.identity?.name
            )
            if (!authorizationTypes.present) {
                throw new AscendUnauthorizedException("No authorization types found")
            }
            PrototypeAuthorization prototypeAuthorization = authorizationTypes.get()
            return  createNewAccessAuthorization(clientAuthorization, prototypeAuthorization)
        } catch (AscendUnauthorizedException ascendUnauthorizedException) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized", ascendUnauthorizedException)
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server exception during granting", exception)
        }
    }

    Authorization exchangeRefreshJwt(String refreshJwt) {
        try {
            Refresh refresh = jwtService.jwt2refresh(refreshJwt, jwtService.jwtRefreshKeyPublic)
            Optional<PrototypeAuthorization> prototypeAccessOptional = prototypeAuthorizationRepository.findAccessByRefresh(refresh.serverNamespace, refresh.name)
            if (!prototypeAccessOptional.present) {
                throw new AscendUnauthorizedException("No access authorizations associated with this refresh")
            }
            if (refresh.expiryDate.before(new Date())) {
                throw new AscendForbiddenException("Expired Refresh Authorization")
            }
            PrototypeAuthorization prototypeAccess = prototypeAccessOptional.get()
            Authorization accessAuthorization = prototypeConverter.convertAccessAuthorization(prototypeAccess, refresh.clientNamespace)
            accessAuthorization.scope = prototypeConverter.convertScope(prototypeAccess.scopes.first())
            accessAuthorization.identity = prototypeConverter.convertIdentity(prototypeAccess.identities.first())
            accessAuthorization.identity.authenticatedCredentials = refresh.authenticatedCredentials
            accessAuthorization.jwt = jwtService.authorization2jwt(accessAuthorization, jwtService.jwtAccessKeyPrivate)
            if (Optional.ofNullable(prototypeAccess.refresh).present) {
                if (prototypeAccess.refresh.isRenewable) {
                    accessAuthorization.refresh = prototypeConverter.convertRefresh(prototypeAccess, refresh.clientNamespace)
                    accessAuthorization.refresh.jwt = jwtService.refresh2jwt(accessAuthorization.refresh, jwtService.jwtRefreshKeyPrivate)
                }
            }
            authorizationRepository.saveAndFlush(accessAuthorization)
            return accessAuthorization
        } catch (AscendUnauthorizedException ascendUnauthorizedException) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized", ascendUnauthorizedException)
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server exception during granting", exception)
        }
    }

    Authorization createNewAccessAuthorization(Authorization clientAuthorization, PrototypeAuthorization prototypeAuthorization) {
        Map<String, String> prerequisiteAuthenticatedCredentials
        if (prototypeAuthorization.prerequisites.size() != 0) {
            if (clientAuthorization.prerequisite == null) {
                throw new AscendUnauthorizedException("Missing prerequisite")
            }
            Authorization clientPrerequisiteAuthorization = jwtService.jwt2authorization(clientAuthorization.prerequisite.jwt, jwtService.jwtAccessKeyPublic)
            if (clientPrerequisiteAuthorization.expiryDate.before(new Date())) {
                throw new AscendUnauthorizedException("Expired prerequisite")
            }
            Boolean prerequisiteFound = false
            prerequisiteAuthenticatedCredentials = clientPrerequisiteAuthorization?.identity?.authenticatedCredentials
            for (PrototypeAuthorization prerequisiteAuthorizationType in prototypeAuthorization.prerequisites) {
                if (prerequisiteAuthorizationType.name == clientPrerequisiteAuthorization.name) {
                    validatePrerequisite(clientPrerequisiteAuthorization, prerequisiteAuthorizationType)
                    prerequisiteFound = true
                    break
                }
            }
            if (!prerequisiteFound) {
                throw new AscendUnauthorizedException("Wrong prerequisite type")
            }
        }
        Map<String, String> authenticatedCredentials = new HashMap<String, String>()
        for (PrototypeAuthentication authenticationType in prototypeAuthorization.identities.first().authentications) {
            Boolean authenticationFound = false
            for (Authentication authentication in clientAuthorization.identity.authentications) {
                if (authentication.name == authenticationType.name) {
                    authenticationFound = true
                    Map<String, String> additionalAuthenticatedCredentials = commonAuthenticationValidation(authentication)
                    if (additionalAuthenticatedCredentials != null) {
                        for (authenticatedCredentialsName in additionalAuthenticatedCredentials.keySet()) {
                            if (authenticatedCredentials.containsKey(authenticatedCredentialsName)) {
                                if (authenticatedCredentials.get(authenticatedCredentialsName) !=
                                        additionalAuthenticatedCredentials.get(authenticatedCredentialsName)) {
                                    throw new AscendUnauthorizedException("Inconsistent authenticated credentials")
                                }
                            } else {
                                authenticatedCredentials.put(authenticatedCredentialsName, additionalAuthenticatedCredentials.get(authenticatedCredentialsName))
                            }
                        }
                    }
                    break
                }
            }
            if (!authenticationFound) {
                throw new AscendUnauthorizedException("Missing authentication")
            }
        }
        if (prerequisiteAuthenticatedCredentials != null) {
            authenticatedCredentials.each {
                if (prerequisiteAuthenticatedCredentials.get(it.key) != it.value) {
                    throw new AscendUnauthorizedException("Inconsistent prerequisite")
                }
            }
        }
        Authorization authorization = prototypeConverter.convertAccessAuthorization(prototypeAuthorization, clientAuthorization.clientNamespace)
        authorization.scope = prototypeConverter.convertScope(prototypeAuthorization.scopes.first())
        authorization.identity = prototypeConverter.convertIdentity(prototypeAuthorization.identities.first())
        authorization.identity.authenticatedCredentials = authenticatedCredentials
        authorization.jwt = jwtService.authorization2jwt(authorization, jwtService.jwtAccessKeyPrivate)
        if (Optional.ofNullable(prototypeAuthorization.refresh).present) {
            authorization.refresh = prototypeConverter.convertRefresh(prototypeAuthorization, clientAuthorization.clientNamespace)
            authorization.refresh.authenticatedCredentials = authorization.identity.authenticatedCredentials
            authorization.refresh.jwt = jwtService.refresh2jwt(authorization.refresh, jwtService.jwtRefreshKeyPrivate)
        }
        authorizationRepository.saveAndFlush(authorization)
        return authorization
    }

    Map<String, String> commonAuthenticationValidation(Authentication authentication) {
        try {
            Map<String, String> authenticatedCredentials = serverAuthenticationService.validateAuthentication(authentication)
            return authenticatedCredentials
        } finally {
            authentication.authenticationData?.privateCredentials = null
        }
    }

    void validatePrerequisite(Authorization authorization, PrototypeAuthorization prototypeAuthorization) {
        if (authorization.serverNamespace != prototypeAuthorization.serverNamespace) {
            throw new AscendUnauthorizedException("Wrong prerequisite server namespace")
        }
        if (!prototypeAuthorization.identities.collect { it.name }.contains(authorization.identity.name)) {
            throw new AscendUnauthorizedException("Wrong prerequisite identity")
        }
        if (!prototypeAuthorization.scopes.collect { it.name }.contains(authorization.scope.name)) {
            throw new AscendUnauthorizedException("Wrong prerequisite scope")
        }
    }

}
