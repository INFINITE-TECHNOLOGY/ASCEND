package io.infinite.ascend.common

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.granting.model.Authorization
import io.infinite.ascend.granting.model.enums.AuthorizationPurpose
import io.infinite.ascend.other.AscendException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyPair
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

@Component
@Slf4j
@BlackBox(level = CarburetorLevel.ERROR)
class JwtManager {

    @Autowired
    ObjectMapper objectMapper

    Key jwtAccessKeyPrivate

    Key jwtAccessKeyPublic

    Key jwtRefreshKeyPrivate

    Key jwtRefreshKeyPublic

    @PostConstruct
    void init() {
        if (System.getenv("useEnvKeys") == "true") {
            jwtAccessKeyPrivate = loadKeyFromEnv("jwtAccessKeyPrivate")
            jwtAccessKeyPublic = loadKeyFromEnv("jwtAccessKeyPublic")
            jwtRefreshKeyPrivate = loadKeyFromEnv("jwtRefreshKeyPrivate")
            jwtRefreshKeyPublic = loadKeyFromEnv("jwtRefreshKeyPublic")
        } else {
            log.info("No keys defined in the environment, generating keys. This will take some time.")
            log.info("To speed-up Ascend loading, consider defining the keys in the environment.")
            KeyPair keyPairAccess = Keys.keyPairFor(SignatureAlgorithm.RS512)
            jwtAccessKeyPrivate = keyPairAccess.getPrivate()
            jwtAccessKeyPublic = keyPairAccess.getPublic()
            KeyPair keyPairRefresh = Keys.keyPairFor(SignatureAlgorithm.RS512)
            jwtRefreshKeyPrivate = keyPairRefresh.getPrivate()
            jwtRefreshKeyPublic = keyPairRefresh.getPublic()
            log.info("Finished generating the keys.")
        }
    }

    Key loadKeyFromEnv(String keyName) {
        byte[] keyBytes = Base64.getDecoder().decode(System.getenv(keyName))
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA512")
    }

    @CompileDynamic
    @BlackBox(level = CarburetorLevel.METHOD)
    Authorization accessJwt2authorization(String iJwt) {
        Jwt jwt = Jwts.parser().setSigningKey(jwtAccessKeyPrivate).parse(iJwt)
        Authorization authorization = objectMapper.readValue(unzip(jwt.getBody() as String), Authorization.class)
        authorization.jwt = iJwt
        return authorization
    }

    @BlackBox(level = CarburetorLevel.METHOD)
    String unzip(String compressed) {
        def inflaterStream = new GZIPInputStream(new ByteArrayInputStream(compressed.decodeBase64()))
        def uncompressedStr = inflaterStream.getText(StandardCharsets.UTF_8.name())
        return uncompressedStr
    }

    @BlackBox(level = CarburetorLevel.METHOD)
    def zip(String s) {
        def targetStream = new ByteArrayOutputStream()
        def zipStream = new GZIPOutputStream(targetStream)
        zipStream.write(s.getBytes(StandardCharsets.UTF_8.name()))
        zipStream.close()
        def zippedBytes = targetStream.toByteArray()
        targetStream.close()
        return zippedBytes.encodeBase64()
    }

    @CompileDynamic
    @BlackBox(level = CarburetorLevel.METHOD)
    void setJwt(Authorization iAuthorization) {
        String body = zip(objectMapper.writeValueAsString(iAuthorization))
        Key key
        if (iAuthorization.purpose == AuthorizationPurpose.ACCESS) {
            key = jwtAccessKeyPrivate
        } else if (iAuthorization.purpose == AuthorizationPurpose.REFRESH) {
            key = jwtRefreshKeyPrivate
        } else {
            throw new AscendException("Unsupported authorization purpose " + iAuthorization.purpose.stringValue())
        }
        iAuthorization.jwt = Jwts.builder()
                .setPayload(body)
                .signWith(SignatureAlgorithm.HS512, key).compact()
    }

}
