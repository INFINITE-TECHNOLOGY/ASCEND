package io.infinite.ascend.web.controllers

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.model.AscendKeyPair
import io.infinite.ascend.common.services.JwtService
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

import java.security.KeyPair

@Controller
@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
class KeyPairController {

    @Autowired
    JwtService jwtService

    @GetMapping(value = "/public/keyPair")
    @ResponseBody
    @CompileDynamic
    @BlackBox(level = BlackBoxLevel.ERROR)
    AscendKeyPair getAscendKeyPair() {
        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS512)
        AscendKeyPair ascendKeyPair = new AscendKeyPair()
        ascendKeyPair.privateKey = jwtService.privateKeyToString(keyPair.getPrivate())
        ascendKeyPair.publicKey = jwtService.publicKeyToString(keyPair.getPublic())
        return ascendKeyPair
    }

}
