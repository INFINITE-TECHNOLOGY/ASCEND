package io.infinite.ascend.granting.server.authentication

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.granting.server.entities.TrustedPublicKey
import io.infinite.ascend.granting.server.repositories.TrustedPublicKeyRepository
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.apache.commons.lang3.time.FastDateFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
@Service
class ClientJwtValidator implements AuthenticationValidator {

    @Autowired
    JwtService jwtService

    @Autowired
    TrustedPublicKeyRepository trustedAppRepository

    @Override
    Map<String, String> validate(Map<String, String> publicCredentials, Map<String, String> privateCredentials) {
        String ascendClientPublicKeyName = publicCredentials.get("ascendClientPublicKeyName")
        String clientJwt = privateCredentials.get("clientJwt")
        if (ascendClientPublicKeyName == null || clientJwt == null) {
            throw new AscendUnauthorizedException("Missing ascendClientPublicKeyName or clientJwt")
        }
        Optional<TrustedPublicKey> trustedAppOptional = trustedAppRepository.findByName(ascendClientPublicKeyName)
        if (!trustedAppOptional.present) {
            throw new AscendUnauthorizedException("Key $ascendClientPublicKeyName is not trusted.")
        }
        Authorization selfIssuedAuthorization = jwtService.jwt2authorization(clientJwt, jwtService.loadPublicKeyFromHexString(trustedAppOptional.get().publicKey))
        log.debug(FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date()))
        log.debug(FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS").format(selfIssuedAuthorization.expiryDate))
        if (selfIssuedAuthorization.expiryDate.before(new Date())) {
            throw new AscendUnauthorizedException("Expired clientJwt")
        }
        return ["ascendClientPublicKeyName": publicCredentials.get("ascendClientPublicKeyName")]
    }

}
