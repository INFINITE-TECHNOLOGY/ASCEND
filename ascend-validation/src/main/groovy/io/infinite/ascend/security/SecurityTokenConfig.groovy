package io.infinite.ascend.security

import io.infinite.ascend.granting.components.JwtManager
import io.infinite.ascend.repositories.UsageRepository
import io.infinite.blackbox.BlackBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import javax.servlet.http.HttpServletResponse

/**
 * https://github.com/OmarElGabry/microservices-spring-boot/blob/master/spring-eureka-zuul/src/main/java/com/eureka/zuul/security/SecurityTokenConfig.java
 */
@EnableWebSecurity
class SecurityTokenConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    JwtManager jwtManager

    @Autowired
    UsageRepository usageRepository

    @Override
    @BlackBox
    protected void configure(HttpSecurity http) throws Exception {
        JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter = new JwtTokenAuthenticationFilter(
                jwtManager: jwtManager, usageRepository: usageRepository)
        http
                .csrf().disable()
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
                .addFilterAfter(jwtTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        // authorization requests config
                .authorizeRequests()
        // allow all who are accessing "auth" service
                .antMatchers(HttpMethod.POST, "/auth").permitAll()
        // must be an admin if trying to access admin area (authentication is also required here)
                .antMatchers("/gallery" + "/admin/**").hasRole("ADMIN")
        // Any other request must be authenticated
                .anyRequest().authenticated()
    }

}