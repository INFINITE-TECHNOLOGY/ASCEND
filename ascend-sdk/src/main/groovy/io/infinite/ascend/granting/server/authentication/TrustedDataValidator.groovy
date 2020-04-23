package io.infinite.ascend.granting.server.authentication

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.server.entities.TrustedPublicKey
import io.infinite.ascend.granting.server.repositories.TrustedPublicKeyRepository
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.apache.commons.lang3.time.FastDateFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class TrustedDataValidator implements AuthenticationValidator {

    @Autowired
    JwtService jwtService

    @Autowired
    TrustedPublicKeyRepository trustedAppRepository

    @Override
    Map<String, String> authenticate(Authentication authentication, Authorization authorization) {
        String keyName = authentication.authenticationData.publicCredentials.get("keyName")
        String selfIssuedJwt = authentication.authenticationData.privateCredentials.get("selfIssuedJwt")
        if (keyName == null || selfIssuedJwt == null) {
            log.warn("Missing keyName or selfIssuedJwt")
            authentication.isSuccessful = false
            return null
        }
        Optional<TrustedPublicKey> trustedAppOptional = trustedAppRepository.findByKeyName(keyName)
        if (!trustedAppOptional.present) {
            log.warn("Application $keyName is not trusted.")
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
        return ["keyName": authentication.authenticationData.publicCredentials.get("keyName")]
    }

}
