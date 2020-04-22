package io.infinite.ascend.common.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.jsonwebtoken.CompressionCodecs
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.apache.shiro.codec.Hex
import org.springframework.stereotype.Service

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

@Service
@Slf4j
@BlackBox(level = CarburetorLevel.METHOD)
class JwtService {

    ObjectMapper objectMapper = new ObjectMapper(
            new YAMLFactory()
                    .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .disable(YAMLGenerator.Feature.SPLIT_LINES)
                    .enable(YAMLGenerator.Feature.INDENT_ARRAYS)
    )

    @CompileDynamic
    @BlackBox(level = CarburetorLevel.ERROR)
    Authorization jwt2Authorization(String iJwt, PublicKey publicKey) {
        Jwt jwt = Jwts.parser().setSigningKey(publicKey).parse(iJwt)
        Authorization authorization = objectMapper.readValue(jwt.getBody() as String, Authorization.class)
        authorization.jwt = iJwt
        return authorization
    }

    @CompileDynamic
    @BlackBox(level = CarburetorLevel.ERROR)
    String authorization2Jwt(Authorization authorization, PrivateKey privateKey) {
        String body = objectMapper.writeValueAsString(authorization)
        log.debug(body)
        String jwt = Jwts.builder()
                .setPayload(body)
                .signWith(privateKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact()
        log.debug(jwt)
        return jwt
    }

    @BlackBox(level = CarburetorLevel.NONE)
    PKCS8EncodedKeySpec loadSpecFromHexStringPrivate(String hexString) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Hex.decode(hexString))
        return pkcs8EncodedKeySpec
    }

    @BlackBox(level = CarburetorLevel.NONE)
    X509EncodedKeySpec loadSpecFromHexStringPublic(String hexString) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Hex.decode(hexString))
        return x509EncodedKeySpec
    }

    @BlackBox(level = CarburetorLevel.NONE)
    PrivateKey loadPrivateKeyFromHexString(String hexString) {
        return KeyFactory.getInstance(SignatureAlgorithm.RS512.getFamilyName()).generatePrivate(loadSpecFromHexStringPrivate(hexString))
    }

    PublicKey loadPublicKeyFromHexString(String hexString) {
        return KeyFactory.getInstance(SignatureAlgorithm.RS512.getFamilyName()).generatePublic(loadSpecFromHexStringPublic(hexString))
    }

    @BlackBox(level = CarburetorLevel.NONE)
    String privateKeyToString(PrivateKey privateKey) {
        return Hex.encodeToString(privateKey.getEncoded())
    }

    String publicKeyToString(PublicKey publicKey) {
        return Hex.encodeToString(publicKey.getEncoded())
    }

}
