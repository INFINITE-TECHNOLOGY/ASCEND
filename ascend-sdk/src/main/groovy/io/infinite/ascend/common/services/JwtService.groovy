package io.infinite.ascend.common.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Refresh
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.common.repositories.RefreshRepository
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.jsonwebtoken.CompressionCodecs
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.apache.shiro.codec.Hex
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

@Service
@Slf4j
@BlackBox(level = BlackBoxLevel.METHOD)
class JwtService {

    @Value('${jwtAccessKeyPublic}')
    String jwtAccessKeyPublicString

    @Value('${jwtAccessKeyPrivate}')
    String jwtAccessKeyPrivateString

    @Value('${jwtRefreshKeyPublic}')
    String jwtRefreshKeyPublicString

    @Value('${jwtRefreshKeyPrivate}')
    String jwtRefreshKeyPrivateString

    PublicKey jwtAccessKeyPublic

    PrivateKey jwtAccessKeyPrivate

    PublicKey jwtRefreshKeyPublic

    PrivateKey jwtRefreshKeyPrivate

    @Autowired
    RefreshRepository refreshRepository

    @Autowired
    AuthorizationRepository authorizationRepository

    @PostConstruct
    void initKeys() {
        if (jwtAccessKeyPublicString != "") jwtAccessKeyPublic = loadPublicKeyFromHexString(jwtAccessKeyPublicString)
        if (jwtAccessKeyPrivateString != "") jwtAccessKeyPrivate = loadPrivateKeyFromHexString(jwtAccessKeyPrivateString)
        if (jwtRefreshKeyPublicString != "") jwtRefreshKeyPublic = loadPublicKeyFromHexString(jwtRefreshKeyPublicString)
        if (jwtRefreshKeyPrivateString != "") jwtRefreshKeyPrivate = loadPrivateKeyFromHexString(jwtRefreshKeyPrivateString)
    }

    ObjectMapper objectMapper = new ObjectMapper(
            new YAMLFactory()
                    .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .disable(YAMLGenerator.Feature.SPLIT_LINES)
                    .enable(YAMLGenerator.Feature.INDENT_ARRAYS)
    )

    @CompileDynamic
    @BlackBox(level = BlackBoxLevel.ERROR)
    Authorization jwt2authorization(String jwtString, PublicKey publicKey) {
        Jwt jwt = Jwts.parser().setSigningKey(publicKey).parse(jwtString)
        Authorization authorization = objectMapper.readValue(jwt.getBody() as String, Authorization.class)
        Optional<Authorization> authorizationOptional = authorizationRepository.findByGuid(authorization.guid)
        if (!authorizationOptional.present) {
            throw new AscendUnauthorizedException("Authorization not found by GUID ${authorization.guid?.toString()}")
        }
        Authorization existingAuthorization = authorizationOptional.get()
        existingAuthorization.jwt = jwtString
        return existingAuthorization
    }

    @CompileDynamic
    @BlackBox(level = BlackBoxLevel.ERROR)
    Refresh jwt2refresh(String jwtString, PublicKey publicKey) {
        Jwt jwt = Jwts.parser().setSigningKey(publicKey).parse(jwtString)
        Refresh refresh = objectMapper.readValue(jwt.getBody() as String, Refresh.class)
        Optional<Refresh> refreshOptional = refreshRepository.findByGuid(refresh.guid)
        if (!refreshOptional.present) {
            throw new AscendUnauthorizedException("Refresh not found by GUID ${refresh.guid?.toString()}")
        }
        Refresh existingRefresh = refreshOptional.get()
        existingRefresh.jwt = jwtString
        return existingRefresh
    }

    @CompileDynamic
    @BlackBox(level = BlackBoxLevel.ERROR)
    String authorization2jwt(Authorization authorization, PrivateKey privateKey) {
        String body = objectMapper.writeValueAsString(new Authorization(
                guid: authorization.guid
        ))
        log.debug(body)
        String jwt = Jwts.builder()
                .setPayload(body)
                .signWith(privateKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact()
        log.debug(jwt)
        return jwt
    }

    @CompileDynamic
    @BlackBox(level = BlackBoxLevel.ERROR)
    String refresh2jwt(Refresh refresh, PrivateKey privateKey) {
        String body = objectMapper.writeValueAsString(new Refresh(
                guid: refresh.guid
        ))
        log.debug(body)
        String jwt = Jwts.builder()
                .setPayload(body)
                .signWith(privateKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact()
        log.debug(jwt)
        return jwt
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @BlackBox(level = BlackBoxLevel.NONE)
    PKCS8EncodedKeySpec loadSpecFromHexStringPrivate(String hexString) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Hex.decode(hexString))
        return pkcs8EncodedKeySpec
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @BlackBox(level = BlackBoxLevel.NONE)
    X509EncodedKeySpec loadSpecFromHexStringPublic(String hexString) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Hex.decode(hexString))
        return x509EncodedKeySpec
    }

    @BlackBox(level = BlackBoxLevel.NONE)
    PrivateKey loadPrivateKeyFromHexString(String hexString) {
        return KeyFactory.getInstance(SignatureAlgorithm.RS512.getFamilyName()).generatePrivate(loadSpecFromHexStringPrivate(hexString))
    }

    PublicKey loadPublicKeyFromHexString(String hexString) {
        return KeyFactory.getInstance(SignatureAlgorithm.RS512.getFamilyName()).generatePublic(loadSpecFromHexStringPublic(hexString))
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @BlackBox(level = BlackBoxLevel.NONE)
    String privateKeyToString(PrivateKey privateKey) {
        return Hex.encodeToString(privateKey.getEncoded())
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    String publicKeyToString(PublicKey publicKey) {
        return Hex.encodeToString(publicKey.getEncoded())
    }

}
