package io.infinite.ascend.web.security


import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.validation.client.services.ClientAuthorizationValidationService
import io.infinite.ascend.validation.other.AscendForbiddenException
import io.infinite.ascend.validation.other.AscendUnauthorizedException
import io.infinite.ascend.validation.server.services.ServerAuthorizationValidationService
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.HttpRequest
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class LocalAuthorizationValidationService extends ClientAuthorizationValidationService {

    @Autowired
    ServerAuthorizationValidationService serverAuthorizationValidationService

    Integer validateAscendHttpRequest(String jwt, Claim claim) {
        try {
            serverAuthorizationValidationService.validateJwtClaim(jwt, claim)
            return 200
        } catch (AscendUnauthorizedException ascendException) {
            log.warn("Unauthorized access attempt", ascendException)
            return 401
        } catch (AscendForbiddenException ascendException) {
            log.warn("Expired authorization.", ascendException)
            return 403
        } catch (Exception e) {
            log.error("Exception during validation.", e)
            return 500
        }
    }

}
