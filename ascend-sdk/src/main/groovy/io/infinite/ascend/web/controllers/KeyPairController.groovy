package io.infinite.ascend.web.controllers

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.model.AscendKeyPair
import io.infinite.ascend.common.services.JwtService
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

import java.security.KeyPair

@Controller
@Slf4j
class KeyPairController {

    @Autowired
    JwtService jwtService

    @GetMapping(value = "/ascend/public/keyPair")
    @ResponseBody
    @CompileDynamic
    @BlackBox(level = CarburetorLevel.ERROR)
    AscendKeyPair getAscendKeyPair() {
        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS512)
        AscendKeyPair ascendKeyPair = new AscendKeyPair()
        ascendKeyPair.privateKey = jwtService.privateKeyToString(keyPair.getPrivate())
        ascendKeyPair.publicKey = jwtService.publicKeyToString(keyPair.getPublic())
        return ascendKeyPair
    }

}
