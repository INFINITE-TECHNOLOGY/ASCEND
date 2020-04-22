package io.infinite.ascend.granting.client.services

import io.infinite.ascend.granting.client.authentication.ClientAuthenticationModule
import io.infinite.ascend.common.entities.Authentication
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
@BlackBox(level = CarburetorLevel.METHOD)
class ClientAuthenticationService {

    @Autowired
    ApplicationContext applicationContext

    void authenticate(Authentication authentication) {
        ClientAuthenticationModule clientAuthentication = applicationContext.getBean("client" + authentication.name, ClientAuthenticationModule.class)
        authentication.authenticationData = clientAuthentication.authenticate()
    }

}
