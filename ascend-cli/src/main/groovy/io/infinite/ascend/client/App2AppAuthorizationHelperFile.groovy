package io.infinite.ascend.client


import groovy.util.logging.Slf4j
import io.infinite.ascend.common.JwtManager
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel

import java.security.PrivateKey

@Slf4j
@BlackBox(level = CarburetorLevel.METHOD)
class App2AppAuthorizationHelperFile extends App2AppAuthorizationHelperBase {

    File privateKeyFile

    String ascendGrantingUrl

    private App2AppAuthorizationHelperFile() {}

    App2AppAuthorizationHelperFile(File privateKeyFile, String ascendGrantingUrl) {
        this.privateKeyFile = privateKeyFile
        this.ascendGrantingUrl = ascendGrantingUrl
    }

    @Override
    PrivateKey loadPrivateKey(JwtManager jwtManager, String clientAppName) {
        return jwtManager.loadPrivateKeyFromHexString(privateKeyFile.text)
    }

}
