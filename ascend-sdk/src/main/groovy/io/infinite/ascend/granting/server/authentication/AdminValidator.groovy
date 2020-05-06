package io.infinite.ascend.granting.server.authentication

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.server.repositories.TrustedPublicKeyRepository
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.HttpRequest
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class AdminValidator implements AuthenticationValidator {

    @Autowired
    JwtService jwtService

    @Autowired
    TrustedPublicKeyRepository trustedAppRepository

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    @Value('${orbitUrl}')
    String orbitUrl

    @Override
    Map<String, String> validateAuthentication(Authentication authentication) {
        senderDefaultHttps.expectStatus(
                new HttpRequest(
                        url: "$orbitUrl/orbit/public/validateAdministratorGuid/${authentication.authenticationData.publicCredentials.get("registrationGuid")}",
                        method: "POST",
                        headers: [
                                "Content-Type" : "application/json",
                                "Accept"       : "application/json"
                        ]
                ), 200
        )
        return [
                "isAdmin": "true"
        ]
    }

}
