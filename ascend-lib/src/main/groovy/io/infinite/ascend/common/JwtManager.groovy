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
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

@Component
@Slf4j
@BlackBox(level = CarburetorLevel.ERROR)
class JwtManager {

    @Autowired
    ObjectMapper objectMapper

    PrivateKey jwtAccessKeyPrivate

    PublicKey jwtAccessKeyPublic

    PrivateKey jwtRefreshKeyPrivate

    PublicKey jwtRefreshKeyPublic

    @PostConstruct
    void init() {
        if (System.getenv("useEnvKeys") == "true") {
            jwtAccessKeyPrivate = loadPrivateKeyFromEnv("jwtAccessKeyPrivate")
            jwtAccessKeyPublic = loadPublicKeyFromEnv("jwtAccessKeyPublic")
            jwtRefreshKeyPrivate = loadPrivateKeyFromEnv("jwtRefreshKeyPrivate")
            jwtRefreshKeyPublic = loadPublicKeyFromEnv("jwtRefreshKeyPublic")
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

    PrivateKey loadPrivateKeyFromEnv(String keyName) {
        byte[] clear = Base64.getDecoder().decode(System.getenv(keyName))
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear)
        KeyFactory fact = KeyFactory.getInstance("RSA")
        PrivateKey priv = fact.generatePrivate(keySpec)
        Arrays.fill(clear, (byte) 0)
        return priv
    }


    PublicKey loadPublicKeyFromEnv(String keyName) {
        byte[] data = Base64.getDecoder().decode(System.getenv(keyName))
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data)
        KeyFactory fact = KeyFactory.getInstance("RSA")
        return fact.generatePublic(spec)
    }

    String privateKeyToString(PrivateKey privateKey) {
        KeyFactory fact = KeyFactory.getInstance("RSA")
        PKCS8EncodedKeySpec spec = fact.getKeySpec(privateKey,
                PKCS8EncodedKeySpec.class)
        byte[] packed = spec.getEncoded()
        String key64 = Base64.encoder.encodeToString(packed)

        Arrays.fill(packed, (byte) 0)
        return key64
    }


    String publicKeyToString(PublicKey publicKey) {
        KeyFactory fact = KeyFactory.getInstance("RSA")
        X509EncodedKeySpec spec = fact.getKeySpec(publicKey,
                X509EncodedKeySpec.class)
        return Base64.encoder.encodeToString(spec.getEncoded())
    }

}
