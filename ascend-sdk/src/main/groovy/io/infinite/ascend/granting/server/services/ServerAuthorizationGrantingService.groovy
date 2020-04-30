package io.infinite.ascend.granting.server.services

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
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
            Authorization refreshAuthorization = jwtService.jwt2Authorization(refreshJwt, jwtService.jwtRefreshKeyPublic)
            if (!refreshAuthorization.isRefresh) {
                throw new AscendUnauthorizedException("Not a refresh authorization")
            }
            Optional<PrototypeAuthorization> prototypeAccessOptional = prototypeAuthorizationRepository.findAccessByRefresh(refreshAuthorization.serverNamespace, refreshAuthorization.name)
            if (!prototypeAccessOptional.present) {
                throw new AscendUnauthorizedException("No access authorizations associated with this refresh")
            }
            if (refreshAuthorization.expiryDate.before(new Date())) {
                throw new AscendForbiddenException("Expired Refresh Authorization")
            }
            PrototypeAuthorization prototypeAccess = prototypeAccessOptional.get()
            Authorization accessAuthorization = prototypeConverter.convertAuthorization(prototypeAccess, refreshAuthorization.clientNamespace)
            accessAuthorization.scope = prototypeConverter.convertScope(prototypeAccess.scopes.first())
            accessAuthorization.identity = prototypeConverter.convertIdentity(prototypeAccess.identities.first())
            accessAuthorization.identity.authenticatedCredentials = refreshAuthorization.identity.authenticatedCredentials
            accessAuthorization.jwt = jwtService.authorization2Jwt(accessAuthorization, jwtService.jwtAccessKeyPrivate)
            if (Optional.ofNullable(prototypeAccess.refresh).present) {
                accessAuthorization.refresh = prototypeConverter.convertAuthorization(prototypeAccess.refresh, refreshAuthorization.clientNamespace)
                accessAuthorization.refresh.scope = prototypeConverter.convertScope(prototypeAccess.scopes.first())
                accessAuthorization.refresh.identity = prototypeConverter.convertIdentity(prototypeAccess.identities.first())
                accessAuthorization.refresh.identity.authenticatedCredentials = refreshAuthorization.identity.authenticatedCredentials
                accessAuthorization.refresh.jwt = jwtService.authorization2Jwt(accessAuthorization.refresh, jwtService.jwtRefreshKeyPrivate)
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
            Authorization clientPrerequisiteAuthorization = jwtService.jwt2Authorization(clientAuthorization.prerequisite.jwt, jwtService.jwtAccessKeyPublic)
            Boolean prerequisiteFound = false
            prerequisiteAuthenticatedCredentials = clientPrerequisiteAuthorization?.identity?.authenticatedCredentials
            for (PrototypeAuthorization prerequisiteAuthorizationType in prototypeAuthorization.prerequisites) {
                if (prerequisiteAuthorizationType.name == clientPrerequisiteAuthorization.name) {
                    Authorization prerequisiteAuthorization = createNewAccessAuthorization(clientPrerequisiteAuthorization, prerequisiteAuthorizationType)
                    if (prerequisiteAuthorization.expiryDate.before(new Date())) {
                        throw new AscendUnauthorizedException("Expired prerequisite")
                    }
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
        Authorization authorization = prototypeConverter.convertAuthorization(prototypeAuthorization, clientAuthorization.clientNamespace)
        authorization.scope = prototypeConverter.convertScope(prototypeAuthorization.scopes.first())
        authorization.identity = prototypeConverter.convertIdentity(prototypeAuthorization.identities.first())
        authorization.identity.authenticatedCredentials = authenticatedCredentials
        authorization.isRefresh = false
        authorization.jwt = jwtService.authorization2Jwt(authorization, jwtService.jwtAccessKeyPrivate)
        if (Optional.ofNullable(prototypeAuthorization.refresh).present) {
            authorization.refresh = prototypeConverter.convertAuthorization(prototypeAuthorization.refresh, clientAuthorization.clientNamespace)
            authorization.refresh.scope = prototypeConverter.convertScope(prototypeAuthorization.scopes.first())
            authorization.refresh.identity = prototypeConverter.convertIdentity(prototypeAuthorization.identities.first())
            authorization.refresh.identity.authenticatedCredentials = authenticatedCredentials
            authorization.refresh.isRefresh = true
            authorization.refresh.jwt = jwtService.authorization2Jwt(authorization.refresh, jwtService.jwtRefreshKeyPrivate)
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

}
