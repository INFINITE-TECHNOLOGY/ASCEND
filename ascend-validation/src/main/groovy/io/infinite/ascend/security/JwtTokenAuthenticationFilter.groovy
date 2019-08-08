package io.infinite.ascend.security

import com.netflix.zuul.http.HttpServletRequestWrapper
import groovy.util.logging.Slf4j
import io.infinite.ascend.entities.Usage
import io.infinite.ascend.granting.components.JwtManager
import io.infinite.ascend.granting.model.Authorization
import io.infinite.ascend.other.AscendException
import io.infinite.ascend.repositories.UsageRepository
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.stream.Collectors

/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/JwtTokenAuthenticationFilter.java
 */

@Slf4j
@BlackBox
class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    JwtManager jwtManager

    UsageRepository usageRepository

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization")
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response)
                return
            }
            String jwt = authorizationHeader.replace("Bearer ", "")
            Authorization authorization = jwtManager.accessJwt2authorization(jwt)
            validateAuthorization(authorization, request)
            PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken =
                    new PreAuthenticatedAuthenticationToken(authorization.identity, authorization.identity?.authentications)
            SecurityContextHolder.getContext().setAuthentication(preAuthenticatedAuthenticationToken)
        }
        catch (Exception e) {
            log.warn("Exception during validation", e)
            SecurityContextHolder.clearContext()
        } finally {
            chain.doFilter(request, response)
        }
    }

    @BlackBox(level = CarburetorLevel.METHOD)
    void validateAuthorization(Authorization iAuthorization, HttpServletRequest iHttpServletRequest) {
        if (iAuthorization.expiryDate.before(new Date())) {
            throw new AscendException("Expired Authorization")
        }
        for (grant in iAuthorization.scope.grants) {
            if (grant.httpMethod.stringValue().toLowerCase() == iHttpServletRequest.method.toLowerCase()) {
                if (grant.urlRegex != null) {
                    String processedUrlRegex = replaceSubstitutes(grant.urlRegex, iAuthorization)
                    if (iHttpServletRequest.requestURL.toString().matches(processedUrlRegex)) {
                        log.debug("URL matched regex.")
                        if (grant.bodyRegex != null) {
                            String processedBodyRegex = replaceSubstitutes(grant.bodyRegex, iAuthorization)
                            HttpServletRequestWrapper httpServletRequestWrapper = new HttpServletRequestWrapper(iHttpServletRequest)
                            //todo: check for DDOS (never ending input stream)
                            String body = httpServletRequestWrapper.getReader().lines().collect(Collectors.toList())
                            if (body.matches(processedBodyRegex)) {
                                log.debug("Body matched regex.")
                                if (iAuthorization.maxUsageCount > 0) {
                                    if (usageRepository.findByAuthorizationId(iAuthorization.id).size() >= iAuthorization.maxUsageCount) {
                                        throw new AscendException("Exceeded maximum usage count")
                                    }
                                    Usage l_usage = new Usage(authorizationId: iAuthorization.id, usageDate: new Date())
                                    usageRepository.saveAndFlush(l_usage)
                                    log.debug("Authorized")
                                }
                            }
                        }
                    }
                }
            }
        }
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