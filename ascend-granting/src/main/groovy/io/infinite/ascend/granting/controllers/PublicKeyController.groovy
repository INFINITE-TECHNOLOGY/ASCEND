package io.infinite.ascend.granting.controllers

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.JwtManager
import io.infinite.ascend.granting.AuthorizationGranting
import io.infinite.ascend.granting.model.AscendKeyPair
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

import java.security.KeyPair

@Controller
@Slf4j
class PublicKeyController {

    @Autowired
    JwtManager jwtManager

    @GetMapping(value = "/ascend/publicKey", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CompileDynamic
    @BlackBox(level = CarburetorLevel.ERROR)
    /**
     * Returns Ascend JWT Access Public Key
     */
    String getPublicKey() {
        return Base64.encoder.encodeToString(jwtManager.jwtAccessKeyPublic.encoded)
    }

}
