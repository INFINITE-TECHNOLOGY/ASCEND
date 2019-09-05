package io.infinite.ascend.validation.controllers

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.other.AscendException
import io.infinite.ascend.validation.AuthorizationValidator
import io.infinite.ascend.validation.model.AscendHttpRequest
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
    AuthorizationValidator authorizationValidator

    @PostMapping(value = "/ascend/validation")
    @ResponseBody
    @CompileDynamic
    @BlackBox(level = CarburetorLevel.METHOD)
    AscendHttpRequest postHttpRequest(@RequestBody AscendHttpRequest ascendHttpRequest) {
        try {
            ascendHttpRequest.authorization = authorizationValidator.validateAuthorizationHeader(ascendHttpRequest.authorizationHeader,
                    ascendHttpRequest.incomingUrl,
                    ascendHttpRequest.method,
                    ascendHttpRequest.body)
            ascendHttpRequest.status = 200
            return ascendHttpRequest
        } catch (AscendException ascendException) {
            ascendHttpRequest.status = 401
            ascendHttpRequest.statusDescription = ascendException.message
            return ascendHttpRequest
        } catch (Exception e) {
            log.error("Exception during validation.", e)
            ascendHttpRequest.status = 401
            ascendHttpRequest.statusDescription = "Unauthorized"
            return ascendHttpRequest
        }
    }

}
