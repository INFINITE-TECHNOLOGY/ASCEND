package io.infinite.ascend.client


import groovy.util.logging.Slf4j
import io.infinite.ascend.common.JwtManager
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel

import java.security.PrivateKey

@Slf4j
@BlackBox(level = CarburetorLevel.METHOD)
class App2AppAuthorizationHelperEnv extends App2AppAuthorizationHelperBase {

    @Override
    PrivateKey loadPrivateKey(JwtManager jwtManager, String clientAppName) {
        return jwtManager.loadPrivateKeyFromHexString(System.getenv("PRIVATE_KEY_OF_" + clientAppName))
    }

    @Override
    String getAscendGrantingUrl() {
        return System.getenv("GRANTING_URL")
    }
}
