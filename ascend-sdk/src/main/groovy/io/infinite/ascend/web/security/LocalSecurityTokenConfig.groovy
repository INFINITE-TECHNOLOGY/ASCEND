package io.infinite.ascend.web.security

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

import javax.servlet.http.HttpServletResponse

/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/SecurityTokenConfig.java
 */
@EnableWebSecurity
@Slf4j
@BlackBox(level = BlackBoxLevel.METHOD)
class LocalSecurityTokenConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    LocalJwtTokenAuthenticationFilter jwtTokenAuthenticationFilter

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
        // make sure we use stateless session session won't be used to store user's state.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
        // handle an authorized attempts
                .exceptionHandling().authenticationEntryPoint({
            req, rsp, e ->
                rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        })
                .and()
        // Add a filter to validate the tokens with every request
                .addFilterAfter(jwtTokenAuthenticationFilter, AbstractPreAuthenticatedProcessingFilter.class)
        // authorization requests config
                .authorizeRequests()
                .antMatchers("/public/**").permitAll()
                .antMatchers("/ascend/public/**").permitAll()
                .anyRequest().authenticated()
    }

}