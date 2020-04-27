package io.infinite.ascend.granting.client.services

import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.granting.client.authentication.AuthenticationPreparator
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
@BlackBox(level = CarburetorLevel.METHOD)
class ClientAuthenticationPreparationService {

    @Autowired
    ApplicationContext applicationContext

    void prepareAuthentication(Authentication authentication) {
        AuthenticationPreparator clientAuthentication
        try {
            clientAuthentication = applicationContext.getBean(authentication.name + "Preparator", AuthenticationPreparator.class)
        } catch (NoSuchBeanDefinitionException noSuchBeanDefinitionException) {
            throw new AscendUnauthorizedException("Authentication Preparator not found: ${authentication.name + "Validator"}", noSuchBeanDefinitionException)
        }
        authentication.authenticationData = clientAuthentication.prepareAuthentication()
    }

}
