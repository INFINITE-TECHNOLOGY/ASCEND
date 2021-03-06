package io.infinite.ascend.granting.server.authentication

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.server.repositories.TrustedPublicKeyRepository
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.http.HttpRequest
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
@Service
class UserValidator implements AuthenticationValidator {

    @Autowired
    JwtService jwtService

    @Autowired
    TrustedPublicKeyRepository trustedAppRepository

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    @Value('${orbitUrl}')
    String orbitUrl

    @Override
    Map<String, String> validate(Map<String, String> publicCredentials, Map<String, String> privateCredentials) {
        senderDefaultHttps.expectStatus(
                new HttpRequest(
                        url: "$orbitUrl/orbit/public/user/${publicCredentials.get("userGuid")}",
                        method: "GET",
                        headers: [
                                "Content-Type": "application/json",
                                "Accept"      : "application/json"
                        ]
                ), 200
        )
        return [
                //todo: validate with phone - by returning phone
                "userGuid": publicCredentials.get("userGuid")
        ]
    }

}
