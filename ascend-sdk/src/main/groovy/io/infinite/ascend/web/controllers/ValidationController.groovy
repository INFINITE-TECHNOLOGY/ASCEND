package io.infinite.ascend.web.controllers

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.web.security.LocalAuthorizationValidationService
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@BlackBox
@Slf4j
class ValidationController {

    @Autowired
    LocalAuthorizationValidationService localAuthorizationValidationService

    @PostMapping(value = "/ascend/public/validation")
    @ResponseBody
    @CompileDynamic
    @BlackBox(level = CarburetorLevel.METHOD)
    Authorization validateClaim(
            @RequestBody Claim claim
    ) {
        return localAuthorizationValidationService.authorizeClaim(claim)
    }

}
