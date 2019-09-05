package io.infinite.ascend.validation


import groovy.util.logging.Slf4j
import io.infinite.ascend.common.JwtManager
import io.infinite.ascend.granting.model.Authorization
import io.infinite.ascend.other.AscendException
import io.infinite.ascend.validation.entities.Usage
import io.infinite.ascend.validation.repositories.UsageRepository
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@BlackBox
@Slf4j
@Component
class AuthorizationValidator {

    @Autowired
    JwtManager jwtManager

    @Autowired
    UsageRepository usageRepository

    Authorization validateAuthorizationHeader(String authorizationHeader, String incomingUrl, String method, String body) {
        String jwt = authorizationHeader.replace("Bearer ", "")
        Authorization authorization = jwtManager.accessJwt2authorization(jwt)
        validateAuthorization(authorization, incomingUrl, method, body)
        return authorization
    }

    @BlackBox(level = CarburetorLevel.METHOD)
    void validateAuthorization(Authorization authorization, String incomingUrl, String method, String body) {
        if (authorization.expiryDate.before(new Date())) {
            throw new AscendException("Expired Authorization")
        }
        for (grant in authorization.scope.grants) {
            if (grant.httpMethod.toLowerCase() == method.toLowerCase()) {
                if (grant.urlRegex != null) {
                    String processedUrlRegex = replaceSubstitutes(grant.urlRegex, authorization)
                    log.debug("Processed URL regex", processedUrlRegex)
                    if (incomingUrl.matches(processedUrlRegex)) {
                        log.debug("URL matched regex.")
                        if (grant.bodyRegex != null) {
                            //todo: check for DDOS (never ending input stream)
                            String processedBodyRegex = replaceSubstitutes(grant.bodyRegex, authorization)
                            log.debug("Body", body)
                            log.debug("Processed body regex", processedUrlRegex)
                            if (!body.matches(processedBodyRegex)) {
                                log.debug("Body does not match regex")
                                throw new AscendException("Unauthorized")
                            }
                        }
                        if (authorization.maxUsageCount > 0) {
                            if (usageRepository.findByAuthorizationId(authorization.id).size() >= authorization.maxUsageCount) {
                                throw new AscendException("Exceeded maximum usage count")
                            }
                            Usage usage = new Usage(authorizationId: authorization.id, usageDate: new Date())
                            usageRepository.saveAndFlush(usage)
                            log.debug("Authorized")
                            return
                        }
                    }
                }
            }
        }
        log.debug("No matching grant found")
        throw new AscendException("Unauthorized")
    }

    String replaceSubstitutes(String iStringWithSubstitutes, Authorization iAuthorization) {
        String processedString = iStringWithSubstitutes
        iAuthorization.identity?.authenticatedCredentials?.each {
            processedString = processedString.replace("%" + it.key + "%", it.value)
        }
        return processedString
    }

}
