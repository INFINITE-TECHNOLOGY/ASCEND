package io.infinite.ascend.granting.server.authentication

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.server.entities.TrustedApp
import io.infinite.ascend.granting.server.repositories.TrustedAppRepository
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.apache.commons.lang3.time.FastDateFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class ServerApplicationData implements ServerAuthenticationModule {

    @Autowired
    JwtService jwtService

    @Autowired
    TrustedAppRepository trustedAppRepository

    @Override
    Map<String, String> authenticate(Authentication authentication, Authorization authorization) {
        String appName = authentication.authenticationData.publicCredentials.get("appName")
        String selfIssuedJwt = authentication.authenticationData.privateCredentials.get("selfIssuedJwt")
        if (appName == null || selfIssuedJwt == null) {
            log.warn("Missing appName or selfIssuedJwt")
            authentication.isSuccessful = false
            return null
        }
        Optional<TrustedApp> trustedAppOptional = trustedAppRepository.findByAppName(appName)
        if (!trustedAppOptional.present) {
            log.warn("Application $appName is not trusted.")
            authentication.isSuccessful = false
            return null
        }
        Authorization selfIssuedAuthorization = jwtService.jwt2Authorization(selfIssuedJwt, jwtService.loadPublicKeyFromHexString(trustedAppOptional.get().publicKey))
        log.debug(FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date()))
        log.debug(FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS").format(selfIssuedAuthorization.expiryDate))
        if (selfIssuedAuthorization.expiryDate.before(new Date())) {
            log.warn("Expired selfIssuedJwt")
            authentication.isSuccessful = false
            return null
        }
        authentication.isSuccessful = true
        return ["appName": authentication.authenticationData.publicCredentials.get("appName")]
    }

}
