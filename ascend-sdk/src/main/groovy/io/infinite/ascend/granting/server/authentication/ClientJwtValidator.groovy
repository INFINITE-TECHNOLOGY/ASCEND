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
class ClientJwtValidator implements AuthenticationValidator {

    @Autowired
    JwtService jwtService

    @Autowired
    TrustedPublicKeyRepository trustedAppRepository

    @Override
    Map<String, String> authenticate(Authentication authentication, Authorization authorization) {
        String clientPublicKeyName = authentication.authenticationData.publicCredentials.get("clientPublicKeyName")
        String clientJwt = authentication.authenticationData.privateCredentials.get("clientJwt")
        if (clientPublicKeyName == null || clientJwt == null) {
            log.warn("Missing clientPublicKeyName or clientJwt")
            authentication.isSuccessful = false
            return null
        }
        Optional<TrustedPublicKey> trustedAppOptional = trustedAppRepository.findByName(clientPublicKeyName)
        if (!trustedAppOptional.present) {
            log.warn("Key $clientPublicKeyName is not trusted.")
            authentication.isSuccessful = false
            return null
        }
        Authorization selfIssuedAuthorization = jwtService.jwt2Authorization(clientJwt, jwtService.loadPublicKeyFromHexString(trustedAppOptional.get().publicKey))
        log.debug(FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date()))
        log.debug(FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS").format(selfIssuedAuthorization.expiryDate))
        if (selfIssuedAuthorization.expiryDate.before(new Date())) {
            log.warn("Expired clientJwt")
            authentication.isSuccessful = false
            return null
        }
        authentication.isSuccessful = true
        return ["clientPublicKeyName": authentication.authenticationData.publicCredentials.get("clientPublicKeyName")]
    }

}
