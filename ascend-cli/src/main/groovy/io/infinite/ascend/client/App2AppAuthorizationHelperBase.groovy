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
import io.infinite.http.HttpRequest
import io.infinite.http.HttpResponse
import io.infinite.http.SenderDefaultHttps

import java.security.PrivateKey

@Slf4j
@BlackBox(level = CarburetorLevel.METHOD)
abstract class App2AppAuthorizationHelperBase {

    abstract PrivateKey loadPrivateKey(JwtManager jwtManager, String clientAppName)

    abstract String getAscendGrantingUrl()

    @CompileDynamic
    Authorization createApp2AppAuthorization(String clientAppName, String scopeName) {
        JwtManager jwtManager = new JwtManager()
        jwtManager.jwtAccessKeyPrivate = loadPrivateKey(jwtManager, clientAppName)
        Authorization selfIssuedAuthorization = new Authorization()
        selfIssuedAuthorization.creationDate = new Date()
        use(TimeCategory) {
            selfIssuedAuthorization.setExpiryDate(selfIssuedAuthorization.creationDate + 60.seconds)
        }
        selfIssuedAuthorization.durationSeconds = 60
        selfIssuedAuthorization.purpose = AuthorizationPurpose.ACCESS
        jwtManager.setJwt(selfIssuedAuthorization)
        log.debug(selfIssuedAuthorization.jwt)
        HttpRequest ascendRequest = new HttpRequest(
                url: ascendGrantingUrl,
                headers: ["content-type": "text/yaml"],
                method: "POST",
                body: """---
name: App2app
identity:
  name: Trusted Application
  authentications:
  - name: JWT
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
