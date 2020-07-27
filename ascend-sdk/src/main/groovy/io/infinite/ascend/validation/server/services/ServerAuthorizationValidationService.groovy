package io.infinite.ascend.validation.server.services

import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.common.exceptions.AscendForbiddenException
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.common.repositories.ClaimRepository
import io.infinite.ascend.common.services.JwtService
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
@Service
class ServerAuthorizationValidationService {

    @Autowired
    JwtService jwtService

    @Autowired
    AuthorizationRepository authorizationRepository

    @Autowired
    ClaimRepository claimRepository

    @Autowired
    ApplicationContext applicationContext

    Authorization validateClaim(Claim claim) {
        try {
            Authorization authorization = jwtService.jwt2authorization(claim.jwt, jwtService.jwtAccessKeyPublic)
            validateAuthorizationClaim(authorization, claim)
            return authorization
        } catch (AscendUnauthorizedException ascendUnauthorizedException) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access attempt", ascendUnauthorizedException)
        } catch (AscendForbiddenException ascendForbiddenException) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Expired authorization", ascendForbiddenException)
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server exception during validation", exception)
        }
    }

    Authorization validateAuthorizationClaim(Authorization authorization, Claim claim) {
        if (authorization.expiryDate.before(new Date())) {
            throw new AscendForbiddenException("Expired Authorization")
        }
        for (grant in authorization.scope.grants) {
            if (grant.httpMethod.toLowerCase() == claim.method.toLowerCase()) {
                if (grant.urlRegex != null) {
                    String processedUrlRegex = replaceSubstitutes(grant.urlRegex, authorization.authorizedCredentials)
                    log.debug("Processed URL regex", processedUrlRegex)
                    if (claim.url.matches(processedUrlRegex)) {
                        log.debug("URL matched regex.")
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

    String replaceSubstitutes(String string, Map<String, String> authorizedCredentials) {
        String processedString = string
        authorizedCredentials.each {
            processedString = processedString.replace("%" + it.key + "%", it.value)
        }
        return processedString
    }

}
