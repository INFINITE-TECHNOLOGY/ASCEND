package io.infinite.ascend.granting.controllers

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.JwtManager
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

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
        return jwtManager.publicKeyToString(jwtManager.jwtAccessKeyPublic)
    }

}
