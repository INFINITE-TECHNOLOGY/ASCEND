package io.infinite.ascend.granting

import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.JwtManager
import io.infinite.ascend.config.entities.PersistentAuthentication
import io.infinite.ascend.config.entities.PersistentAuthorization
import io.infinite.ascend.config.repositories.PersistentAuthorizationRepository
import io.infinite.ascend.granting.model.Authentication
import io.infinite.ascend.granting.model.Authorization
import io.infinite.ascend.granting.model.Grant
import io.infinite.ascend.granting.model.Scope
import io.infinite.ascend.granting.model.enums.AuthenticationStatus
import io.infinite.ascend.granting.model.enums.AuthorizationErrorCode
import io.infinite.ascend.granting.model.enums.AuthorizationPurpose
import io.infinite.ascend.granting.model.enums.AuthorizationStatus
import io.infinite.blackbox.BlackBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@BlackBox
@Slf4j
@Component
class AuthorizationGranting {

    @Value('${ascendAuthenticationPluginsDir}')
    String ascendAuthenticationPluginsDir

    @Autowired
    PersistentAuthorizationRepository authorizationTypeRepository

    @Autowired
    JwtManager jwtManager

    Authorization authorizationGranting(Authorization iAuthorization) {
        try {
            Set<PersistentAuthorization> authorizationTypes = authorizationTypeRepository.findForGranting(
                    iAuthorization.name,
                    iAuthorization.scope?.name,
                    iAuthorization.identity?.name
            )
            if (authorizationTypes.size() == 0) {
                log.debug("No authorization types found")
                failure(iAuthorization, AuthorizationErrorCode.OTHER)
                return iAuthorization
            }
            PersistentAuthorization authorizationType = authorizationTypes.first()
            grantByType(iAuthorization, authorizationType)
            return iAuthorization
        } catch (Exception e) {
            log.warn("Exception during granting", e)
            failure(iAuthorization, AuthorizationErrorCode.OTHER)
            return iAuthorization
        } finally {
            iAuthorization.identity?.authentications?.each { Authentication authentication ->
                authentication.authenticationData?.privateCredentials = null
            }
        }
    }

    void grantByType(Authorization iAuthorization, PersistentAuthorization iAuthorizationType) {
        Map<String, String> prerequisiteAuthenticatedCredentials
        if (iAuthorizationType.prerequisiteAuthorizationTypes.size() != 0) {
            if (iAuthorization.prerequisiteJwt == null) {
                log.debug("Missing prerequisite")
                failure(iAuthorization, AuthorizationErrorCode.OTHER)
                return
            }
            Authorization prerequisiteAuthorization = jwtManager.accessJwt2authorization(iAuthorization.prerequisiteJwt)
            Boolean prerequisiteFound = false
            prerequisiteAuthenticatedCredentials = prerequisiteAuthorization?.identity?.authenticatedCredentials
            for (PersistentAuthorization prerequisiteAuthorizationType in iAuthorizationType.prerequisiteAuthorizationTypes) {
                if (prerequisiteAuthorizationType.name == prerequisiteAuthorization.name) {
                    grantByType(prerequisiteAuthorization, prerequisiteAuthorizationType)
                    if (prerequisiteAuthorization.status != AuthorizationStatus.SUCCESSFUL) {
                        log.debug("Failed prerequisite")
                        failure(iAuthorization, AuthorizationErrorCode.OTHER)
                        return
                    }
                    if (prerequisiteAuthorization.expiryDate.before(new Date())) {
                        log.debug("Expired prerequisite")
                        failure(iAuthorization, AuthorizationErrorCode.OTHER)
                        return
                    }
                    prerequisiteFound = true
                    break
                }
            }
            if (!prerequisiteFound) {
                log.debug("Wrong prerequisite type")
                failure(iAuthorization, AuthorizationErrorCode.OTHER)
                return
            }
        }
        for (PersistentAuthentication authenticationType in iAuthorizationType.identityTypes.first().authenticationTypes) {
            Boolean authenticationFound = false
            for (Authentication authentication in iAuthorization.identity.authentications) {
                if (authentication.name == authenticationType.name) {
                    authenticationFound = true
                    Map<String, String> authenticatedCredentials = commonAuthenticationValidation(authentication, iAuthorization)
                    if (authentication.status != AuthenticationStatus.SUCCESSFUL) {
                        log.debug("Failed authentication")
                        failure(iAuthorization, AuthorizationErrorCode.AUTHENTICATION_FAILED)
                        return
                    } else {
                        if (authenticatedCredentials != null) {
                            for (authenticatedCredentialsName in authenticatedCredentials.keySet()) {
                                if (iAuthorization.identity.authenticatedCredentials.containsKey(authenticatedCredentialsName)) {
                                    if (iAuthorization.identity.authenticatedCredentials.get(authenticatedCredentialsName) !=
                                            authenticatedCredentials.get(authenticatedCredentialsName)) {
                                        log.debug("Inconsistent authenticated credentials")
                                        failure(iAuthorization, AuthorizationErrorCode.OTHER)
                                        return
                                    }
                                } else {
                                    iAuthorization.identity.authenticatedCredentials.put(authenticatedCredentialsName, authenticatedCredentials.get(authenticatedCredentialsName))
                                }
                            }
                        }
                    }
                    break
                }
            }
            if (!authenticationFound) {
                log.debug("Missing authentication")
                failure(iAuthorization, AuthorizationErrorCode.OTHER)
                return
            }
        }
        if (prerequisiteAuthenticatedCredentials != null) {
            iAuthorization.identity.authenticatedCredentials.each {
                if (prerequisiteAuthenticatedCredentials.get(it.key) != it.value) {
                    log.debug("Inconsistent prerequisite")
                    failure(iAuthorization, AuthorizationErrorCode.OTHER)
                    return
                }
            }
        }
        log.debug("Success")
        iAuthorization.durationSeconds = iAuthorizationType.durationSeconds
        iAuthorization.maxUsageCount = iAuthorizationType.maxUsageCount
        iAuthorization.scope = new Scope(
                grants: iAuthorizationType.scopes.first().grantTypes.collect {
                    new Grant(
                            bodyRegex: it.bodyRegex,
                            urlRegex: it.urlRegex,
                            httpMethod: it.httpMethod
                    )
                }.toSet()
        )
        iAuthorization.refreshAuthorization = null
        iAuthorization.purpose = AuthorizationPurpose.ACCESS
        iAuthorization.status = AuthorizationStatus.SUCCESSFUL
        iAuthorization.id = UUID.randomUUID()
        log.debug(iAuthorization.id.toString())
        setExpiryDate(iAuthorization)
        jwtManager.setJwt(iAuthorization)
        if (iAuthorizationType.isRefreshAllowed) {
            iAuthorization.refreshAuthorization = new Authorization()
            iAuthorization.refreshAuthorization.name = iAuthorization.name
            iAuthorization.refreshAuthorization.purpose = AuthorizationPurpose.ACCESS
            iAuthorization.refreshAuthorization.identity = iAuthorization.identity
            iAuthorization.refreshAuthorization.scope = iAuthorization.scope
            iAuthorization.refreshAuthorization.durationSeconds = iAuthorizationType.refreshDurationSeconds
            iAuthorization.refreshAuthorization.maxUsageCount = iAuthorizationType.refreshMaxUsageCount
            setExpiryDate(iAuthorization.refreshAuthorization)
            jwtManager.setJwt(iAuthorization.refreshAuthorization)
        }
    }

    Map<String, String> commonAuthenticationValidation(Authentication iAuthentication, Authorization iAuthorization) {
        Binding binding = new Binding()
        binding.setVariable("authentication", iAuthentication)
        binding.setVariable("authorization", iAuthorization)
        Map<String, String> authenticatedCredentials = getAuthenticationGroovyScriptEngine().run(iAuthentication.name + ".groovy", binding) as Map<String, String>
        iAuthentication.authenticationData?.privateCredentials = null
        return authenticatedCredentials
    }

    @CompileDynamic
    void setExpiryDate(Authorization iAuthorization) {
        iAuthorization.creationDate = new Date()
        use(TimeCategory) {
            iAuthorization.expiryDate = iAuthorization.creationDate + iAuthorization.durationSeconds.seconds
        }
    }

    void failure(Authorization authorization, AuthorizationErrorCode authorizationErrorCode) {
        authorization.status = AuthorizationStatus.FAILED
        authorization.errorCode = authorizationErrorCode
    }

    GroovyScriptEngine getAuthenticationGroovyScriptEngine() {
        return new GroovyScriptEngine(ascendAuthenticationPluginsDir, this.getClass().getClassLoader())
    }

}
