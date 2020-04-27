package io.infinite.ascend.granting.server.services

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.granting.server.authentication.AuthenticationValidator
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
class ServerAuthenticationValidationService {

    @Autowired
    ApplicationContext applicationContext

    Map<String, String> validateAuthentication(Authentication authentication) {
        AuthenticationValidator authenticationValidator
        try {
            authenticationValidator = applicationContext.getBean(authentication.name + "Validator", AuthenticationValidator.class)
        } catch (NoSuchBeanDefinitionException noSuchBeanDefinitionException) {
            throw new AscendUnauthorizedException("Authentication validator not found: ${authentication.name + "Validator"}", noSuchBeanDefinitionException)
        }
        return authenticationValidator.validateAuthentication(authentication)
    }

}
