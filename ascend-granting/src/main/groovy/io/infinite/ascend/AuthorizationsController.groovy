package io.infinite.ascend

import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import io.infinite.ascend.config.entities.AuthenticationType
import io.infinite.ascend.config.entities.AuthorizationType
import io.infinite.ascend.config.repositories.AuthorizationTypeRepository
import io.infinite.ascend.granting.components.JwtManager
import io.infinite.ascend.granting.model.Authentication
import io.infinite.ascend.granting.model.Authorization
import io.infinite.ascend.granting.model.enums.AuthenticationStatus
import io.infinite.ascend.granting.model.enums.AuthorizationErrorCode
import io.infinite.ascend.granting.model.enums.AuthorizationPurpose
import io.infinite.ascend.granting.model.enums.AuthorizationStatus
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@BlackBox
@Slf4j
class AuthorizationsController {

    @Value('${ascendAuthenticationPluginsDir}')
    String ascendAuthenticationPluginsDir

    @Autowired
    AuthorizationTypeRepository authorizationTypeRepository

    @Autowired
    JwtManager jwtManager

    @Memoized
    GroovyScriptEngine getAuthenticationGroovyScriptEngine() {
        return new GroovyScriptEngine(ascendAuthenticationPluginsDir, this.getClass().getClassLoader())
    }

    @PostMapping(value = "/ascend/authorization")
    @ResponseBody
    @CompileDynamic
    @BlackBox(level = CarburetorLevel.METHOD)
    Authorization postAuthorization(@RequestBody Authorization iAuthorization) {
        try {
            Set<AuthorizationType> authorizationTypes = authorizationTypeRepository.findForGranting(
                    iAuthorization.name,
                    iAuthorization.scope?.name,
                    iAuthorization.identity?.name
            )
            if (authorizationTypes.size() == 0) {
                log.debug("No authorization types found")
                failure(iAuthorization, AuthorizationErrorCode.OTHER)
                return iAuthorization
            }
            AuthorizationType authorizationType = authorizationTypes.first()
            commonAuthorizationGranting(iAuthorization, authorizationType)
            return iAuthorization
        } catch (Exception e) {
            log.warn("Exception during granting", e)
            failure(iAuthorization, AuthorizationErrorCode.OTHER)
            return iAuthorization
        } finally {
            iAuthorization.identity?.authentications?.forEach {
                it.authenticationData?.privateDataFieldMap = null
            }
        }
    }

    void commonAuthorizationGranting(Authorization iAuthorization, AuthorizationType iAuthorizationType) {
        if (iAuthorizationType.prerequisiteAuthorizationTypes.size() != 0) {
            if (iAuthorization.prerequisiteJwt == null) {
                log.debug("Missing prerequisite")
                failure(iAuthorization, AuthorizationErrorCode.OTHER)
                return
            }
            Authorization prerequisiteAuthorization = jwtManager.accessJwt2authorization(iAuthorization.prerequisiteJwt)
            for (AuthorizationType prerequisiteAuthorizationType in iAuthorizationType.prerequisiteAuthorizationTypes) {
                commonAuthorizationGranting(prerequisiteAuthorization, prerequisiteAuthorizationType)
            }
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
        }
        for (AuthenticationType authenticationType in iAuthorizationType.identityTypes.first().authenticationTypes) {
            Boolean authenticationFound = false
            for (Authentication authentication in iAuthorization.identity.authentications) {
                if (authentication.name == authenticationType.name) {
                    authenticationFound = true
                    commonAuthenticationValidation(authentication, iAuthorization)
                    if (authentication.status != AuthenticationStatus.SUCCESSFUL) {
                        log.debug("Failed authentication")
                        failure(iAuthorization, AuthorizationErrorCode.AUTHENTICATION_FAILED)
                        return
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
        log.debug("Success")
        iAuthorization.durationSeconds = iAuthorizationType.durationSeconds
        iAuthorization.maxUsageCount = iAuthorizationType.maxUsageCount
        iAuthorization.scope = iAuthorizationType.scopes.first()
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

    void commonAuthenticationValidation(Authentication iAuthentication, Authorization iAuthorization) {
        Binding binding = new Binding()
        binding.setVariable("authentication", iAuthentication)
        binding.setVariable("authorization", iAuthorization)
        getAuthenticationGroovyScriptEngine().run(iAuthentication.name + ".groovy", binding)
        iAuthentication.authenticationData?.privateDataFieldMap = null
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

}
