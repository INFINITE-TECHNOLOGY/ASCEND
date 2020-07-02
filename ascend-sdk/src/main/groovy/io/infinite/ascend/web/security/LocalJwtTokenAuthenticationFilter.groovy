package io.infinite.ascend.web.security

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.common.exceptions.AscendForbiddenException
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.ascend.validation.server.services.ServerAuthorizationValidationService
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.server.ResponseStatusException

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/JwtTokenAuthenticationFilter.java
 */

@Slf4j
@BlackBox(level = BlackBoxLevel.METHOD)
@Component
class LocalJwtTokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    ServerAuthorizationValidationService serverAuthorizationValidationService

    @Override
    @BlackBox(level = BlackBoxLevel.METHOD)
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            String authorizationHeader = request.getHeader("Authorization")
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response)
                return
            }
            String clientUrl
            if (request.getQueryString() != null) {
                clientUrl = request.requestURL
                        .append('?')
                        .append(request.getQueryString())
                        .toString()
            } else {
                clientUrl = request.requestURL
            }
            Claim claim = new Claim(
                    url: clientUrl,
                    jwt: authorizationHeader.replace("Bearer ", ""),
                    method: request.method
            )
            Authorization authorization = serverAuthorizationValidationService.validateClaim(claim)
            PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken =
                    new PreAuthenticatedAuthenticationToken(authorization, claim)
            preAuthenticatedAuthenticationToken.setAuthenticated(true)
            SecurityContextHolder.getContext().setAuthentication(preAuthenticatedAuthenticationToken)
            chain.doFilter(request, response)
        }
        catch (AscendForbiddenException ascendForbiddenException) {
            SecurityContextHolder.clearContext()
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ascendForbiddenException.message, ascendForbiddenException)
        } catch (AscendUnauthorizedException ascendUnauthorizedException) {
            SecurityContextHolder.clearContext()
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ascendUnauthorizedException.message, ascendUnauthorizedException)
        } catch (Exception exception) {
            SecurityContextHolder.clearContext()
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Client exception during validation", exception)
        }
    }

}