package io.infinite.ascend.validation

import com.netflix.zuul.http.HttpServletRequestWrapper
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

import javax.servlet.http.HttpServletRequest
import java.util.stream.Collectors

@BlackBox
@Slf4j
class AuthorizationValidator {

    AccessJwtManager jwtManager

    UsageRepository usageRepository

    Authorization validateAuthorizationHeader(String authorizationHeader, HttpServletRequest iHttpServletRequest) {
        String jwt = authorizationHeader.replace("Bearer ", "")
        Authorization authorization = jwtManager.accessJwt2authorization(jwt)
        validateAuthorization(authorization, iHttpServletRequest)
        return authorization
    }

    @BlackBox(level = CarburetorLevel.METHOD)
    void validateAuthorization(Authorization iAuthorization, HttpServletRequest iHttpServletRequest) {
        if (iAuthorization.expiryDate.before(new Date())) {
            throw new AscendException("Expired Authorization")
        }
        String incomingUrl = iHttpServletRequest.requestURL
                .append('?')
                .append(iHttpServletRequest.getQueryString())
                .toString()
        log.debug("Incoming URL", incomingUrl)
        log.debug("Incoming Method", iHttpServletRequest.method)
        for (grant in iAuthorization.scope.grants) {
            if (grant.httpMethod.toLowerCase() == iHttpServletRequest.method.toLowerCase()) {
                if (grant.urlRegex != null) {
                    String processedUrlRegex = replaceSubstitutes(grant.urlRegex, iAuthorization)
                    log.debug("Processed URL regex", processedUrlRegex)
                    if (incomingUrl.matches(processedUrlRegex)) {
                        log.debug("URL matched regex.")
                        if (grant.bodyRegex != null) {
                            //todo: check for DDOS (never ending input stream)
                            String processedBodyRegex = replaceSubstitutes(grant.bodyRegex, iAuthorization)
                            HttpServletRequestWrapper httpServletRequestWrapper = new HttpServletRequestWrapper(iHttpServletRequest)
                            String body = httpServletRequestWrapper.getReader().lines().collect(Collectors.toList())
                            log.debug("Body", body)
                            log.debug("Processed body regex", processedUrlRegex)
                            if (!body.matches(processedBodyRegex)) {
                                log.debug("Body does not match regex")
                                throw new AscendException("Unauthorized")
                            }
                        }
                        if (iAuthorization.maxUsageCount > 0) {
                            if (usageRepository.findByAuthorizationId(iAuthorization.id).size() >= iAuthorization.maxUsageCount) {
                                throw new AscendException("Exceeded maximum usage count")
                            }
                            Usage usage = new Usage(authorizationId: iAuthorization.id, usageDate: new Date())
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
