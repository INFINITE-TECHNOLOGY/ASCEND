package io.infinite.ascend.validation

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.granting.model.Authorization
import io.infinite.blackbox.BlackBox
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts

import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyStore
import java.util.zip.GZIPInputStream

@Slf4j
@BlackBox
class AccessJwtManager {

    ObjectMapper objectMapper = new ObjectMapper()

    private Key jwtAccessPublicKey

    AccessJwtManager(Key jwtAccessPublicKey) {
        this.jwtAccessPublicKey = jwtAccessPublicKey
    }

    @CompileDynamic
    Authorization accessJwt2authorization(String iJwt) {
        Jwt jwt = Jwts.parser().setSigningKey(jwtAccessPublicKey).parse(iJwt)
        Authorization authorization = objectMapper.readValue(unzip(jwt.getBody() as String), Authorization.class)
        authorization.jwt = iJwt
        return authorization
    }

    String unzip(String compressed) {
        def inflaterStream = new GZIPInputStream(new ByteArrayInputStream(compressed.decodeBase64()))
        def uncompressedStr = inflaterStream.getText(StandardCharsets.UTF_8.name())
        return uncompressedStr
    }

}
