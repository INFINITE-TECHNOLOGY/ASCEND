package io.infinite.ascend.web.security

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

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
                .cors().and().csrf().disable()
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

    /**
     * Taken from https://stackoverflow.com/a/43559441/6784237
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("HEAD",
                "GET", "POST", "PUT", "DELETE", "PATCH"))
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.setAllowCredentials(true);
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}