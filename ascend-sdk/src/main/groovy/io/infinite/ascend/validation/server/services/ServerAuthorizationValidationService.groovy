package io.infinite.ascend.validation.server.services

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.common.repositories.ClaimRepository
import io.infinite.ascend.common.services.JwtService
import io.infinite.ascend.validation.other.AscendForbiddenException
import io.infinite.ascend.validation.other.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class ServerAuthorizationValidationService {

    @Autowired
    JwtService jwtService

    @Autowired
    AuthorizationRepository authorizationRepository

    @Autowired
    ClaimRepository claimRepository

    @Value('${jwtAccessKeyPublic}')
    String jwtAccessKeyPublic

    Authorization authorizeClaim(Claim claim) {
        Authorization authorization = jwtService.jwt2Authorization(claim.jwt, jwtService.loadPublicKeyFromHexString(jwtAccessKeyPublic))
        validateAuthorizationClaim(authorization, claim)
        return authorization
    }

    Authorization validateAuthorizationClaim(Authorization authorization, Claim claim) {
        if (authorization.expiryDate.before(new Date())) {
            throw new AscendForbiddenException("Expired Authorization")
        }
        for (grant in authorization.scope.grants) {
            if (grant.httpMethod.toLowerCase() == claim.method.toLowerCase()) {
                if (grant.urlRegex != null) {
                    String processedUrlRegex = replaceSubstitutes(grant.urlRegex, authorization)
                    log.debug("Processed URL regex", processedUrlRegex)
                    if (claim.url.matches(processedUrlRegex)) {
                        log.debug("URL matched regex.")
                        if (grant.bodyRegex != null) {
                            //todo: check for DDOS (never ending input stream)
                            String processedBodyRegex = replaceSubstitutes(grant.bodyRegex, authorization)
                            log.debug("Body", claim.body)
                            log.debug("Processed body regex", processedUrlRegex)
                            if (!claim.body.matches(processedBodyRegex)) {
                                log.debug("Body does not match regex")
                                throw new AscendUnauthorizedException("Unauthorized")
                            }
                        }
                        Optional<Authorization> existingAuthorization = authorizationRepository.findByGuid(authorization.guid)
                        if (existingAuthorization.isPresent()) {
                            if (authorization.maxUsageCount > 0 && existingAuthorization.get().claims.size() >= authorization.maxUsageCount) {
                                throw new AscendUnauthorizedException("Exceeded maximum usage count")
                            }
                            authorization = existingAuthorization.get()
                        }
                        claim = claimRepository.saveAndFlush(claim)
                        authorization.claims.add(claim)
                        authorization = authorizationRepository.saveAndFlush(authorization)
                        log.debug("Authorized")
                        return authorization
                    }
                }
            }
        }
        log.debug("No matching grant found")
        throw new AscendUnauthorizedException("Unauthorized")
    }

    String replaceSubstitutes(String iStringWithSubstitutes, Authorization iAuthorization) {
        String processedString = iStringWithSubstitutes
        iAuthorization.identity?.authenticatedCredentials?.each {
            processedString = processedString.replace("%" + it.key + "%", it.value)
        }
        return processedString
    }

}
