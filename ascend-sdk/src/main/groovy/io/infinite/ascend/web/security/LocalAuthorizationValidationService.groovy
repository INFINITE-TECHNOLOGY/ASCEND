package io.infinite.ascend.web.security


import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.validation.client.services.ClientAuthorizationValidationService
import io.infinite.ascend.common.exceptions.AscendForbiddenException
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.ascend.validation.server.services.ServerAuthorizationValidationService
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class LocalAuthorizationValidationService extends ClientAuthorizationValidationService {

    @Autowired
    ServerAuthorizationValidationService serverAuthorizationValidationService

    @Override
    Authorization authorizeClaim(String ascendValidationUrl, Claim claim) {
        try {
            return serverAuthorizationValidationService.authorizeClaim(claim)
        } catch (AscendUnauthorizedException ascendUnauthorizedException) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access attempt", ascendUnauthorizedException)
        } catch (AscendForbiddenException ascendForbiddenException) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Expired authorization", ascendForbiddenException)
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server exception during validation", exception)
        }
    }

}
