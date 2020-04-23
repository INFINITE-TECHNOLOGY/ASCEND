package io.infinite.ascend.granting.server.services


import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.granting.server.authentication.AuthenticationValidator
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
@BlackBox(level = CarburetorLevel.METHOD)
class ServerAuthenticationService {

    @Autowired
    ApplicationContext applicationContext

    Map<String, String> authenticate(Authentication authentication, Authorization authorization) {
        AuthenticationValidator serverAuthentication = applicationContext.getBean(authentication.name + "Validator", AuthenticationValidator.class)
        return serverAuthentication.authenticate(authentication, authorization)
    }

}
