package io.infinite.ascend.granting.server.authentication

import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.http.HttpRequest
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
@Service
class KycValidator implements AuthenticationValidator {

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    @Value('${orbitUrl}')
    String orbitUrl

    JsonSlurper jsonSlurper = new JsonSlurper()

    @Override
    @CompileDynamic
    Map<String, String> validate(Map<String, String> publicCredentials, Map<String, String> privateCredentials) {
        def kyc = jsonSlurper.parseText(senderDefaultHttps.expectStatus(
                new HttpRequest(
                        url: "$orbitUrl/orbit/public/user/${publicCredentials.get("userGuid")}/kyc",
                        method: "GET",
                        headers: [
                                "Content-Type": "application/json",
                                "Accept"      : "application/json"
                        ]
                ), 200
        ).body)
        if (!kyc.kycVerify) {
            throw new AscendUnauthorizedException("KYC is not confirmed.")
        }
        return null
    }

}
