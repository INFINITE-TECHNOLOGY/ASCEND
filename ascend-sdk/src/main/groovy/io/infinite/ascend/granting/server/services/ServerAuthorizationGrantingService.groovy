package io.infinite.ascend.granting.server.services

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Refresh
import io.infinite.ascend.common.entities.Scope
import io.infinite.ascend.common.exceptions.AscendForbiddenException
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.common.repositories.RefreshRepository
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.common.services.PrototypeConverter
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthentication
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import io.infinite.ascend.granting.configuration.repositories.PrototypeAuthorizationRepository
import io.infinite.ascend.granting.server.authentication.AuthenticationValidator
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
@Service
class ServerAuthorizationGrantingService {

    @Autowired
    PrototypeAuthorizationRepository authorizationTypeRepository

    @Autowired
    JwtService jwtService

    @Autowired
    AuthorizationRepository authorizationRepository

    @Autowired
    PrototypeConverter prototypeConverter

    @Autowired
    PrototypeAuthorizationRepository prototypeAuthorizationRepository

    @Autowired
    RefreshRepository refreshRepository

    @Autowired
    ApplicationContext applicationContext

    Authorization grantAccessAuthorization(Authorization clientAuthorization) {
        try {
            Optional<PrototypeAuthorization> authorizationTypes = authorizationTypeRepository.findForGranting(
                    clientAuthorization.serverNamespace,
                    clientAuthorization.name,
                    clientAuthorization.identity.name
            )
            if (!authorizationTypes.present) {
                throw new AscendUnauthorizedException("No authorization types found")
            }
            PrototypeAuthorization prototypeAuthorization = authorizationTypes.get()
            return createNewAccessAuthorization(clientAuthorization, prototypeAuthorization)
        } catch (AscendUnauthorizedException ascendUnauthorizedException) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized", ascendUnauthorizedException)
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server exception during granting", exception)
        }
    }

    List<Scope> getScopesForRefresh(PrototypeAuthorization prototypeAuthorization) {
        List<Scope> scopes = []
        prototypeAuthorization.scopes.each {
            scopes += prototypeConverter.convertLegacyScope(it)
        }
        prototypeAuthorization.prerequisites.each {
            scopes += getScopesForRefresh(it)
        }
        return scopes
    }

    Authorization exchangeRefreshJwt(String refreshJwt) {
        try {
            Refresh refresh = jwtService.jwt2refresh(refreshJwt, jwtService.jwtRefreshKeyPublic)
            if (refresh.expiryDate.before(new Date())) {
                throw new AscendForbiddenException("Expired Refresh Authorization")
            }
            Optional<PrototypeAuthorization> prototypeAccessOptional = prototypeAuthorizationRepository.findAccessByRefresh(
                    refresh.serverNamespace,
                    refresh.name,
                    refresh.identityName
            )
            if (!prototypeAccessOptional.present) {
                throw new AscendUnauthorizedException("No access authorizations associated with this refresh")
            }
            PrototypeAuthorization prototypeAccess = prototypeAccessOptional.get()
            Authorization accessAuthorization = prototypeConverter.convertAccessAuthorization(prototypeAccess, refresh.clientNamespace)
            if (refresh.scopeName != null) {
                accessAuthorization.scopes = getScopesForRefresh(prototypeAccess).toSet()
            } else {
                accessAuthorization.scopes.addAll(refresh.scopes)
            }
            accessAuthorization.identity = prototypeConverter.convertIdentity(prototypeAccess.identities.first())
            accessAuthorization.authorizedCredentials.putAll(refresh.refreshCredentials)
            accessAuthorization.jwt = jwtService.authorization2jwt(accessAuthorization, jwtService.jwtAccessKeyPrivate)
            if (Optional.ofNullable(prototypeAccess.refresh).present) {
                if (prototypeAccess.refresh.isRenewable) {
                    accessAuthorization.refresh = prototypeConverter.convertRefresh(prototypeAccess, refresh.clientNamespace)
                    accessAuthorization.refresh.identityName = accessAuthorization.identity.name
                    accessAuthorization.refresh.scopes = accessAuthorization.scopes
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
        Map<String, String> prerequisiteAuthorizedCredentials = new HashMap<String, String>()
        if (prototypeAuthorization.prerequisites.size() != 0) {
            if (clientAuthorization.prerequisite == null) {
                throw new AscendUnauthorizedException("Required prerequisite is missing")
            }
            Authorization clientPrerequisiteAuthorization = jwtService.jwt2authorization(clientAuthorization.prerequisite.jwt, jwtService.jwtAccessKeyPublic)
            if (clientPrerequisiteAuthorization.expiryDate.before(new Date())) {
                throw new AscendUnauthorizedException("Expired prerequisite")
            }
            Boolean prerequisiteFound = false
            for (PrototypeAuthorization prerequisiteAuthorizationType in prototypeAuthorization.prerequisites) {
                if (prerequisiteAuthorizationType.name == clientPrerequisiteAuthorization.name) {
                    validatePrerequisite(clientPrerequisiteAuthorization, prerequisiteAuthorizationType)
                    prerequisiteAuthorizedCredentials = clientPrerequisiteAuthorization.authorizedCredentials
                    prerequisiteFound = true
                    break
                }
            }
            if (!prerequisiteFound) {
                throw new AscendUnauthorizedException("Wrong prerequisite type")
            }
        }
        Map<String, String> authorizedCredentials = new HashMap<String, String>()
        for (PrototypeAuthentication authenticationType in prototypeAuthorization.identities.first().authentications) {
            Boolean authenticationFound = false
            for (Authentication authentication in clientAuthorization.identity.authentications) {
                if (authentication.name == authenticationType.name) {
                    authenticationFound = true
                    Map<String, String> authenticatedCredentials = validateAuthentication(authentication.name, clientAuthorization.identity.publicCredentials, authentication.privateCredentials)
                    safeMerge(authenticatedCredentials, authorizedCredentials)
                    break
                }
            }
            if (!authenticationFound) {
                throw new AscendUnauthorizedException("Missing authentication")
            }
        }
        safeMerge(prerequisiteAuthorizedCredentials, authorizedCredentials)
        Authorization authorization = prototypeConverter.convertAccessAuthorization(prototypeAuthorization, clientAuthorization.clientNamespace)
        authorization.scopes = prototypeConverter.convertScopes(prototypeAuthorization.scopes)
        authorization.identity = prototypeConverter.convertIdentity(prototypeAuthorization.identities.first())
        authorization.authorizedCredentials = authorizedCredentials
        authorization.jwt = jwtService.authorization2jwt(authorization, jwtService.jwtAccessKeyPrivate)
        if (Optional.ofNullable(prototypeAuthorization.refresh).present) {
            authorization.refresh = prototypeConverter.convertRefresh(prototypeAuthorization, clientAuthorization.clientNamespace)
            authorization.refresh.identityName = authorization.identity.name
            authorization.refresh.refreshCredentials = authorization.authorizedCredentials
            authorization.refresh.scopes = authorization.scopes
            authorization.refresh.jwt = jwtService.refresh2jwt(authorization.refresh, jwtService.jwtRefreshKeyPrivate)
        }
        authorizationRepository.saveAndFlush(authorization)
        return authorization
    }

    Map<String, String> validateAuthentication(String authenticationName, Map<String, String> publicCredentials, Map<String, String> privateCredentials) {
        AuthenticationValidator authenticationValidator
        try {
            authenticationValidator = applicationContext.getBean(authenticationName + "Validator", AuthenticationValidator.class)
        } catch (NoSuchBeanDefinitionException noSuchBeanDefinitionException) {
            throw new AscendUnauthorizedException("Authentication validator not found: ${authenticationName + "Validator"}", noSuchBeanDefinitionException)
        }
        Map<String, String> authenticatedCredentials
        try {
            authenticatedCredentials = authenticationValidator.validate(publicCredentials, privateCredentials)
        } catch (Exception exception) {
            throw new AscendUnauthorizedException("Authentication failed", exception)
        }
        return authenticatedCredentials
    }

    void validatePrerequisite(Authorization authorization, PrototypeAuthorization prototypeAuthorization) {
        if (authorization.serverNamespace != prototypeAuthorization.serverNamespace) {
            throw new AscendUnauthorizedException("Wrong prerequisite server namespace")
        }
        if (!prototypeAuthorization.identities.collect { it.name }.contains(authorization.identity.name)) {
            throw new AscendUnauthorizedException("Wrong prerequisite identity")
        }
    }

    void safeMerge(Map<String, String> from, Map<String, String> to) {
        if (from != null) {
            from.each { kFrom, vFrom ->
                to.each { kTo, vTo ->
                    if (kFrom == kTo && vFrom != vTo) {
                        throw new AscendUnauthorizedException("Inconsistent data")
                    }
                }
                to.put(kFrom, vFrom)
            }
        }
    }
}
