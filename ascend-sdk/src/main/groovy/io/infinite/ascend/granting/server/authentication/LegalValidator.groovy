package io.infinite.ascend.granting.server.authentication

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.server.repositories.TrustedPublicKeyRepository
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class LegalValidator implements AuthenticationValidator {

    @Autowired
    JwtService jwtService

    @Autowired
    TrustedPublicKeyRepository trustedAppRepository

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    @Value('${orbitUrl}')
    String orbitUrl

    @Override
    Map<String, String> validate(Map<String, String> publicCredentials, Map<String, String> privateCredentials) {
        return null
    }

}
