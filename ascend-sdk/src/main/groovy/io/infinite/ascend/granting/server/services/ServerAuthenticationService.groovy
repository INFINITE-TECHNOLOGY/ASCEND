package io.infinite.ascend.granting.server.services

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.granting.common.other.AscendException
import io.infinite.ascend.granting.server.authentication.AuthenticationValidator
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
class ServerAuthenticationService {

    @Autowired
    ApplicationContext applicationContext

    Map<String, String> authenticate(Authentication authentication, Authorization authorization) {
        AuthenticationValidator serverAuthentication
        try {
            serverAuthentication = applicationContext.getBean(authentication.name + "Validator", AuthenticationValidator.class)
        } catch (NoSuchBeanDefinitionException noSuchBeanDefinitionException) {
            throw new AscendException("Authentication validator not found: ${authentication.name + "Validator"}", noSuchBeanDefinitionException)
        }
        return serverAuthentication.authenticate(authentication, authorization)
    }

}
