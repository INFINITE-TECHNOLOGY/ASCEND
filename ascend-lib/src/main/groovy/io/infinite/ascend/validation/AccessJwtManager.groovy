package io.infinite.ascend.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.granting.model.Authorization
import io.infinite.ascend.granting.model.enums.AuthorizationPurpose
import io.infinite.ascend.other.AscendException
import io.infinite.blackbox.BlackBox
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm

import javax.annotation.PostConstruct
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyStore
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

@Slf4j
@BlackBox
class AccessJwtManager {

    private String jwtAccessKeystorePath

    private String jwtAccessKeystoreType

    private String jwtAccessKeystorePassword

    private String jwtAccessKeystoreAlias

    ObjectMapper objectMapper = new ObjectMapper()

    private Key jwtAccessKey

    AccessJwtManager(String jwtAccessKeystorePath, String jwtAccessKeystoreType, String jwtAccessKeystorePassword, String jwtAccessKeystoreAlias) {
        this.jwtAccessKeystorePath = jwtAccessKeystorePath
        this.jwtAccessKeystoreType = jwtAccessKeystoreType
        this.jwtAccessKeystorePassword = jwtAccessKeystorePassword
        this.jwtAccessKeystoreAlias = jwtAccessKeystoreAlias
        FileInputStream accessKeyStoreFileInputStream = new FileInputStream(jwtAccessKeystorePath)
        KeyStore accessKeyStore = KeyStore.getInstance(jwtAccessKeystoreType)
        accessKeyStore.load(accessKeyStoreFileInputStream, jwtAccessKeystorePassword.toCharArray())
        jwtAccessKey = accessKeyStore.getKey(jwtAccessKeystoreAlias, jwtAccessKeystorePassword.toCharArray())
    }

    @CompileDynamic
    Authorization accessJwt2authorization(String iJwt) {
        Jwt jwt = Jwts.parser().setSigningKey(jwtAccessKey).parse(iJwt)
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
