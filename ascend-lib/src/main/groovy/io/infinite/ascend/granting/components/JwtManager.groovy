package io.infinite.ascend.granting.components

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.granting.model.Authorization
import io.infinite.ascend.granting.model.enums.AuthorizationPurpose
import io.infinite.ascend.other.AscendException
import io.infinite.blackbox.BlackBox
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyStore
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

@Component
@Slf4j
@BlackBox
class JwtManager {

    @Value('${ascend.jwt.access.keystore.path}')
    private String jwtAccessKeystorePath

    @Value('${ascend.jwt.access.keystore.type}')
    private String jwtAccessKeystoreType

    @Value('${ascend.jwt.access.keystore.password}')
    private String jwtAccessKeystorePassword

    @Value('${ascend.jwt.access.keystore.alias}')
    private String jwtAccessKeystoreAlias

    @Value('${ascend.jwt.refresh.keystore.path}')
    private String jwtRefreshKeystorePath

    @Value('${ascend.jwt.refresh.keystore.type}')
    private String jwtRefreshKeystoreType

    @Value('${ascend.jwt.refresh.keystore.password}')
    private String jwtRefreshKeystorePassword

    @Value('${ascend.jwt.refresh.keystore.alias}')
    private String jwtRefreshKeystoreAlias

    @Value('${ascend.jwt.issuer.name}')
    private String ascendIssuerName

    @Autowired
    ObjectMapper objectMapper

    private Key jwtAccessKey

    private Key jwtRefreshKey

    @PostConstruct
    void init() {
        FileInputStream accessKeyStoreFileInputStream = new FileInputStream(jwtAccessKeystorePath)
        KeyStore accessKeyStore = KeyStore.getInstance(jwtAccessKeystoreType)
        accessKeyStore.load(accessKeyStoreFileInputStream, jwtAccessKeystorePassword.toCharArray())
        jwtAccessKey = accessKeyStore.getKey(jwtAccessKeystoreAlias, jwtAccessKeystorePassword.toCharArray())
        FileInputStream refreshKeyStoreFileInputStream = new FileInputStream(jwtRefreshKeystorePath)
        KeyStore refreshKeyStore = KeyStore.getInstance(jwtRefreshKeystoreType)
        refreshKeyStore.load(refreshKeyStoreFileInputStream, jwtRefreshKeystorePassword.toCharArray())
        jwtRefreshKey = refreshKeyStore.getKey(jwtRefreshKeystoreAlias, jwtRefreshKeystorePassword.toCharArray())
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
    void setJwt(Authorization iAuthorization) {
        String body = zip(objectMapper.writeValueAsString(iAuthorization))
        Key key
        if (iAuthorization.purpose == AuthorizationPurpose.ACCESS) {
            key = jwtAccessKey
        } else if (iAuthorization.purpose == AuthorizationPurpose.REFRESH) {
            key = jwtRefreshKey
        } else {
            throw new AscendException("Unsupported authorization purpose " + iAuthorization.purpose.stringValue())
        }
        iAuthorization.jwt = Jwts.builder()
                .setPayload(body)
                .signWith(SignatureAlgorithm.HS512, key).compact()
    }

}
