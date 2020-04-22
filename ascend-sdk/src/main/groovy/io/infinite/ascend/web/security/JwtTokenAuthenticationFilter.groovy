package io.infinite.ascend.web.security


import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/JwtTokenAuthenticationFilter.java
 */

@Slf4j
@BlackBox(level = CarburetorLevel.METHOD)
@Component
class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    LocalAuthorizationValidationService localAuthorizationValidationService

    @Override
    @BlackBox(level = CarburetorLevel.METHOD)
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        localAuthorizationValidationService.validateServletRequest(request, response, chain)
    }

}