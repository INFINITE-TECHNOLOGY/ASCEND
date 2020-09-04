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
class DdaValidator implements AuthenticationValidator {

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    @Value('${orbitUrl}')
    String orbitUrl

    JsonSlurper jsonSlurper = new JsonSlurper()

    @Override
    @CompileDynamic
    Map<String, String> validate(Map<String, String> publicCredentials, Map<String, String> privateCredentials) {
        def dda = jsonSlurper.parseText(senderDefaultHttps.expectStatus(
                new HttpRequest(
                        url: "$orbitUrl/orbit/public/user/${publicCredentials.get("userGuid")}/dda",
                        method: "GET",
                        headers: [
                                "Content-Type": "application/json",
                                "Accept"      : "application/json"
                        ]
                ), 200
        ).body)
        if (!dda.ddaLinked) {
            throw new AscendUnauthorizedException("DDA is not linked.")
        }
        def ddaList = jsonSlurper.parseText(senderDefaultHttps.expectStatus(
                new HttpRequest(
                        url: "$orbitUrl/orbit/public/user/${publicCredentials.get("userGuid")}/ddaIdSet",
                        method: "GET",
                        headers: [
                                "Content-Type": "application/json",
                                "Accept"      : "application/json"
                        ],
                        readTimeoutSeconds: 45
                ), 200
        ).body)
        return [
                "ddaIdList": "(" + ddaList.collect { it }.join("|") + ")"
        ]
    }

}
