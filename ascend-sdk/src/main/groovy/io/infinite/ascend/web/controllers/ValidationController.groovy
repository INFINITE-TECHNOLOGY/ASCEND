package io.infinite.ascend.web.controllers

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.validation.other.AscendForbiddenException
import io.infinite.ascend.validation.other.AscendUnauthorizedException
import io.infinite.ascend.validation.server.services.ServerAuthorizationValidationService
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseBody

import javax.servlet.http.HttpServletResponse

@Controller
@BlackBox
@Slf4j
class ValidationController {

    @Autowired
    ServerAuthorizationValidationService serverAuthorizationValidationService

    @PostMapping(value = "/ascend/public/validation")
    @ResponseBody
    @CompileDynamic
    @BlackBox(level = CarburetorLevel.METHOD)
    void validateClaim(
            @RequestBody Claim claim
            , HttpServletResponse response
    ) {
        try {
            serverAuthorizationValidationService.validateJwtClaim(claim)
        } catch (AscendUnauthorizedException ascendException) {
            log.warn("Unauthorized access attempt", ascendException)
            response.sendError(401)
        } catch (AscendForbiddenException ascendException) {
            log.warn("Expired authorization.", ascendException)
            response.sendError(403)
        } catch (Exception e) {
            log.error("Exception during validation.", e)
            response.sendError(500)
        }
    }

}
