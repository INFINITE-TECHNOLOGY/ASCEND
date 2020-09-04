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
class SmsOtpValidator implements AuthenticationValidator {

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
                        url: "$orbitUrl/orbit/public/validateOtp",
                        method: "POST",
                        headers: [
                                "Content-Type": "application/json",
                                "Accept"      : "application/json"
                        ],
                        body: """{
	"guid": "${publicCredentials.get("otpGuid")}",
	"otp": "${privateCredentials.get("otp")}"
}"""
                ), 200
        )
        //vulnerability here: phone is not validated...
        return authorizeCredentials(publicCredentials)
    }

    Map<String, String> authorizeCredentials(Map<String, String> publicCredentials) {
        return ["phone": publicCredentials.get("phone")]
    }

}
