package io.infinite.ascend.granting.server.authentication

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.server.entities.TrustedPublicKey
import io.infinite.ascend.granting.server.repositories.TrustedPublicKeyRepository
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.HttpRequest
import io.infinite.http.SenderDefaultHttps
import org.apache.commons.lang3.time.FastDateFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class SmsOtpValidator implements AuthenticationValidator {

    @Autowired
    JwtService jwtService

    @Autowired
    TrustedPublicKeyRepository trustedAppRepository

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    @Override
    Map<String, String> validateAuthentication(Authentication authentication) {
        senderDefaultHttps.expectStatus(
                new HttpRequest(
                        url: "https://orbit-secured.herokuapp.com/orbit/public/validateOtp",
                        method: "POST",
                        headers: [
                                "Content-Type" : "application/json",
                                "Accept"       : "application/json"
                        ],
                        body: """{
	"guid": "${authentication.authenticationData.publicCredentials.get("otpGuid")}",
	"otp": "${authentication.authenticationData.privateCredentials.get("otp")}"
}"""
                ), 200
        )
        return [
                "otpGuid": authentication.authenticationData.publicCredentials.get("otpGuid"),
                "phone": authentication.authenticationData.publicCredentials.get("phone")
        ]
    }

}
