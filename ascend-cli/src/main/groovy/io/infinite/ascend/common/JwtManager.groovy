package io.infinite.ascend.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.granting.model.Authorization
import io.infinite.ascend.granting.model.enums.AuthorizationPurpose
import io.infinite.ascend.other.AscendException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.jsonwebtoken.CompressionCodecs
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.apache.shiro.codec.Hex
import org.springframework.stereotype.Component

import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec

@Component
@Slf4j
class JwtManager {

    ObjectMapper objectMapper

    PrivateKey jwtAccessKeyPrivate

    PublicKey jwtAccessKeyPublic

    PrivateKey jwtRefreshKeyPrivate

    PublicKey jwtRefreshKeyPublic

    JwtManager() {
        YAMLFactory yamlFactory = new YAMLFactory()
        yamlFactory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        yamlFactory.disable(YAMLGenerator.Feature.SPLIT_LINES)
        yamlFactory.enable(YAMLGenerator.Feature.INDENT_ARRAYS)
        objectMapper = new ObjectMapper(yamlFactory)
        if (System.getenv("useEnvKeys") == "true") {
            log.info("Using keys from Environment variables.")
            jwtAccessKeyPrivate = loadPrivateKeyFromHexString(System.getenv("jwtAccessKeyPrivate"))
            jwtAccessKeyPublic = loadPublicKeyFromHexString(System.getenv("jwtAccessKeyPublic"))
            jwtRefreshKeyPrivate = loadPrivateKeyFromHexString(System.getenv("jwtRefreshKeyPrivate"))
            jwtRefreshKeyPublic = loadPublicKeyFromHexString(System.getenv("jwtRefreshKeyPublic"))
        } else {
            if (System.getenv("genDynamicKeys")) {
                log.info("Generating dynamic keys")
                KeyPair keyPairAccess = Keys.keyPairFor(SignatureAlgorithm.RS512)
                jwtAccessKeyPrivate = keyPairAccess.getPrivate()
                jwtAccessKeyPublic = keyPairAccess.getPublic()
                KeyPair keyPairRefresh = Keys.keyPairFor(SignatureAlgorithm.RS512)
                jwtRefreshKeyPrivate = keyPairRefresh.getPrivate()
                jwtRefreshKeyPublic = keyPairRefresh.getPublic()
                log.info("Finished generating the keys.")
            }
        }
    }

    @CompileDynamic
    @BlackBox(level = CarburetorLevel.METHOD)
    Authorization accessJwt2authorization(String iJwt) {
        Jwt jwt = Jwts.parser().setSigningKey(jwtAccessKeyPublic).parse(iJwt)
        Authorization authorization = objectMapper.readValue(jwt.getBody() as String, Authorization.class)
        authorization.jwt = iJwt
        return authorization
    }

    @CompileDynamic
    @BlackBox(level = CarburetorLevel.METHOD)
    void setJwt(Authorization authorization) {
        String body = objectMapper.writeValueAsString(authorization)
        log.debug(body)
        PrivateKey privateKey
        if (authorization.purpose == AuthorizationPurpose.ACCESS) {
            privateKey = jwtAccessKeyPrivate
        } else if (authorization.purpose == AuthorizationPurpose.REFRESH) {
            privateKey = jwtRefreshKeyPrivate
        } else {
            throw new AscendException("Unsupported authorization purpose " + authorization.purpose.stringValue())
        }
        authorization.jwt = Jwts.builder()
                .setPayload(body)
                .signWith(privateKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact()
    }

    PKCS8EncodedKeySpec loadSpecFromHexString(String hexString) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Hex.decode(hexString))
        return pkcs8EncodedKeySpec
    }

    PrivateKey loadPrivateKeyFromHexString(String hexString) {
        return KeyFactory.getInstance(SignatureAlgorithm.RS512.getFamilyName()).generatePrivate(loadSpecFromHexString(hexString))
    }

    PublicKey loadPublicKeyFromHexString(String hexString) {
        return KeyFactory.getInstance(SignatureAlgorithm.RS512.getFamilyName()).generatePublic(loadSpecFromHexString(hexString))
    }

    String privateKeyToString(PrivateKey privateKey) {
        return Hex.encodeToString(privateKey.getEncoded())
    }

    String publicKeyToString(PublicKey publicKey) {
        return Hex.encodeToString(publicKey.getEncoded())
    }

}
