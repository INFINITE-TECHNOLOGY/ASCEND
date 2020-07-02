package io.infinite.ascend.web.controllers

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.services.JwtService
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
class PublicKeyController {

    @Autowired
    JwtService jwtService

    @GetMapping(value = "/public/publicKey")
    @ResponseBody
    @CompileDynamic
    @BlackBox(level = BlackBoxLevel.METHOD)
    String getPublicKey() {
        return jwtService.jwtAccessKeyPublicString
    }

}
