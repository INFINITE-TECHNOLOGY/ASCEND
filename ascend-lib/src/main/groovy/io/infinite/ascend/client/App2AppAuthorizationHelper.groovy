package io.infinite.ascend.client

import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.JwtManager
import io.infinite.ascend.granting.model.Authorization
import io.infinite.ascend.granting.model.enums.AuthorizationPurpose
import io.infinite.ascend.other.AscendException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.pigeon.http.HttpRequest
import io.infinite.pigeon.http.HttpResponse
import io.infinite.pigeon.http.SenderDefaultHttps

@Slf4j
@BlackBox(level = CarburetorLevel.METHOD)
class App2AppAuthorizationHelper {

    @CompileDynamic
    Authorization createApp2AppAuthorization(String clientAppName, String serverAppName, String scopeName) {
        JwtManager jwtManager = new JwtManager()
        jwtManager.jwtAccessKeyPrivate = jwtManager.loadPrivateKeyFromEnv("PRIVATE_KEY_FOR_$serverAppName")
        Authorization selfIssuedAuthorization = new Authorization()
        use(TimeCategory) {
            selfIssuedAuthorization.setExpiryDate(new Date() + 60.seconds)
        }
        selfIssuedAuthorization.purpose = AuthorizationPurpose.ACCESS
        jwtManager.setJwt(selfIssuedAuthorization)
        log.debug(selfIssuedAuthorization.jwt)
        HttpRequest ascendRequest = new HttpRequest(
                url: System.getenv("GRANTING_URL"),
                headers: ["content-type": "text/yaml"],
                method: "POST",
                body: """---
name: Enqueue
identity:
  name: Trusted Application
  authentications:
  - name: App2app
    authenticationData:
      publicCredentials:
        appName: $clientAppName
      privateCredentials:
        selfIssuedJwt: ${selfIssuedAuthorization.jwt}
scope:
  name: $scopeName
"""
        )
        HttpResponse ascendResponse = new HttpResponse()
        new SenderDefaultHttps().sendHttpMessage(ascendRequest, ascendResponse)
        if (ascendResponse.status != 200) {
            throw new AscendException("Failed Ascend Response code")
        }
        try {
            Authorization authorization = jwtManager.objectMapper.readValue(ascendResponse.body, Authorization.class)
            return authorization
        } catch (Exception e) {
            log.error("Exception parsing Ascend JWT body", e)
            throw new AscendException("Exception parsing Ascend JWT body")
        }
    }

}
