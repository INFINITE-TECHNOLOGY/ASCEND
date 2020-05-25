package io.infinite.ascend

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.infinite.ascend.granting.configuration.entities.*
import io.infinite.ascend.granting.configuration.repositories.*
import io.infinite.ascend.granting.server.entities.TrustedPublicKey
import io.infinite.ascend.granting.server.repositories.TrustedPublicKeyRepository
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.time.Duration

@Service
@Slf4j
@BlackBox(level = CarburetorLevel.METHOD)
@CompileStatic
class ConfigInitService {

    @Autowired
    PrototypeGrantRepository grantRepository

    @Autowired
    PrototypeScopeRepository scopeRepository

    @Autowired
    PrototypeAuthenticationRepository authenticationTypeRepository

    @Autowired
    PrototypeIdentityRepository identityTypeRepository

    @Autowired
    PrototypeAuthorizationRepository authorizationTypeRepository

    @Autowired
    PrototypeRefreshRepository prototypeRefreshRepository

    @Autowired
    TrustedPublicKeyRepository trustedPublicKeyRepository

    @PostConstruct
    void initConfig() {
        authorizationTypeRepository.deleteAll()
        trustedPublicKeyRepository.deleteAll()
        trustedPublicKeyRepository.saveAndFlush(new TrustedPublicKey(
                name: "david-it",
                publicKey: "30820222300d06092a864886f70d01010105000382020f003082020a0282020100944d37ae018b8170d49f17be33c7e52fda8bb881f24dce891ecf5a913344d81642dce0b87f3137af1f414f75c3dd36e4a3892acbbeefb7f8644d51eba89384636a63b14eae37d24660b2fed0c63663ecda4a9796fed859e9e00d48ac7e56372ceb533694e4021b0933facfb2daa358994d6eb0c1f192a9f3fd4494c0fcea1f7ffad0ce0274f39c96c608bcde5b0d9fa4e668f4a029aef26815f6b2a7c866621188ccbb5cf4c14874d9743a3783e31b8679a6cb828c26d1856139a72db53d2e601c105642f8482743db7c23cd5a05435324220ed76965196bee833cafaa52476848b46c13f99458880b28b2f1c3a90d0c533564a9564cbd265be682cdb2414a42006789bde79e7c66491011e80450d78107f284da6f53f9fdb24bbc741d7b1e39bb9f5c549e03febf92a748d382e79d01a21bdf7ce3f30400c58fd8a2eef4b385f5edc85c6d9cabdc3e802e543229fa4b0eee3119d239d3cefe49e04249e28ba88f55eabc38b14986d24582eeb0e15fdb607c719f53f177d93301e428914aba4571d06305eb95dfc6c0a5a3ee401cf1677141fb4b758bb110367a37e3af71d381252e13838777b3caddc3d37c7284a0d7c12a9a2e8a9c517b8d76e02afbe43a62b02d27773a8138f7efd3e459cc91eb1edccb6b18245ea3eca0c5cb44baf3e119028e0f61c2282de2d3d864a218871f824a447ab3083ab3a5d233681e4f033c1f0203010001"
        ))
        log.info("Initializing config")
        PrototypeGrant sendOtpSms = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "POST", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/sendOtpSms"))
        PrototypeGrant sendOtpEmail = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "POST", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/sendOtpEmail"))
        PrototypeGrant userGet = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "GET", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/user\\/%phone%"))
        PrototypeGrant userPost = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "POST", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/user\\/%phone%"))
        PrototypeGrant adminFindByPhone = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "GET", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/admin\\/search\\/findByPhone\\?phone=%phone%"))
        PrototypeGrant adminFindByEmail = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "GET", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/admin\\/search\\/findByEmail\\?email=%email%"))
        PrototypeGrant adminGrant = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "POST", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/admin\\/.*"))
        PrototypeGrant adminGrantGet = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "GET", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/admin\\/.*"))
        PrototypeGrant userGrantGet = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "GET", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/user\\/%userGuid%\\/.*"))
        PrototypeGrant phoneGrantGet = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "GET", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/phone\\/%phone%\\/.*"))
        PrototypeGrant userGrantPost = grantRepository.saveAndFlush(new PrototypeGrant(httpMethod: "POST", urlRegex: "https:\\/\\/orbit-it\\.herokuapp\\.com\\/orbit\\/secured\\/user\\/%userGuid%\\/.*"))
        PrototypeScope legalScope = scopeRepository.saveAndFlush(
                new PrototypeScope(
                        name: "legalScope",
                        grants: [
                                sendOtpSms,
                                sendOtpEmail
                        ].toSet()
                )
        )
        PrototypeScope onboardingScope = scopeRepository.saveAndFlush(
                new PrototypeScope(
                        name: "onboardingScope",
                        grants: [
                                userGet,
                                userPost
                        ].toSet()
                )
        )
        PrototypeScope adminOnboardingScope = scopeRepository.saveAndFlush(
                new PrototypeScope(
                        name: "adminOnboardingScope",
                        grants: [
                                adminFindByPhone,
                                adminFindByEmail
                        ].toSet()
                )
        )
        PrototypeScope registeredUserScope = scopeRepository.saveAndFlush(
                new PrototypeScope(
                        name: "registeredUserScope",
                        grants: [
                                userGrantGet,
                                userGrantPost,
                                phoneGrantGet
                        ].toSet()
                )
        )
        PrototypeScope knownCustomerScope = scopeRepository.saveAndFlush(
                new PrototypeScope(
                        name: "knownCustomerScope"
                )
        )
        PrototypeScope adminScope = scopeRepository.saveAndFlush(
                new PrototypeScope(
                        name: "adminScope",
                        grants: [
                                adminGrant,
                                adminGrantGet
                        ].toSet()
                )
        )
        PrototypeAuthentication smsOtp = authenticationTypeRepository.saveAndFlush(
                new PrototypeAuthentication(
                        name: "smsOtp"
                )
        )
        PrototypeAuthentication emailOtp = authenticationTypeRepository.saveAndFlush(
                new PrototypeAuthentication(
                        name: "emailOtp"
                )
        )
        PrototypeAuthentication userAuthentication = authenticationTypeRepository.saveAndFlush(
                new PrototypeAuthentication(
                        name: "user"
                )
        )
        PrototypeAuthentication veriffMe = authenticationTypeRepository.saveAndFlush(
                new PrototypeAuthentication(
                        name: "veriffMe"
                )
        )
        PrototypeAuthentication adminAuthentication = authenticationTypeRepository.saveAndFlush(
                new PrototypeAuthentication(
                        name: "admin"
                )
        )
        PrototypeAuthentication legal = authenticationTypeRepository.saveAndFlush(
                new PrototypeAuthentication(
                        name: "legal"
                )
        )
        PrototypeIdentity legalUser = identityTypeRepository.saveAndFlush(
                new PrototypeIdentity(
                        name: "legalUser",
                        authentications: [
                                legal
                        ].toSet()
                )
        )
        PrototypeIdentity verifiedEmailOwner = identityTypeRepository.saveAndFlush(
                new PrototypeIdentity(
                        name: "emailOwner",
                        authentications: [
                                emailOtp
                        ].toSet()
                )
        )
        PrototypeIdentity verifiedPhoneOwner = identityTypeRepository.saveAndFlush(
                new PrototypeIdentity(
                        name: "verifiedPhoneOwner",
                        authentications: [
                                smsOtp
                        ].toSet()
                )
        )
        PrototypeIdentity registeredUser = identityTypeRepository.saveAndFlush(
                new PrototypeIdentity(
                        name: "registeredUser",
                        authentications: [
                                userAuthentication
                        ].toSet()
                )
        )
        PrototypeIdentity knownCustomer = identityTypeRepository.saveAndFlush(
                new PrototypeIdentity(
                        name: "knownCustomer",
                        authentications: [
                                veriffMe
                        ].toSet()
                )
        )
        PrototypeIdentity admin = identityTypeRepository.saveAndFlush(
                new PrototypeIdentity(
                        name: "admin",
                        authentications: [
                                adminAuthentication
                        ].toSet()
                )
        )
        identityTypeRepository.flush()
        PrototypeRefresh refresh1dayNonRenewable = prototypeRefreshRepository.saveAndFlush(new PrototypeRefresh(
                durationSeconds: Duration.ofDays(1).seconds.toInteger(),
                isRenewable: false
        ))
        PrototypeRefresh refresh30daysNonRenewable = prototypeRefreshRepository.saveAndFlush(new PrototypeRefresh(
                durationSeconds: Duration.ofDays(30).seconds.toInteger(),
                isRenewable: false
        ))
        PrototypeRefresh refresh30daysRenewable = prototypeRefreshRepository.saveAndFlush(new PrototypeRefresh(
                durationSeconds: Duration.ofDays(30).seconds.toInteger(),
                isRenewable: true
        ))
        PrototypeAuthorization legalAuthorization = authorizationTypeRepository.saveAndFlush(
                new PrototypeAuthorization(name: "legalAuthorization",
                        identities: [
                                legalUser
                        ].toSet(),
                        scopes: [
                                legalScope
                        ].toSet(),
                        durationSeconds: Duration.ofMinutes(30).seconds.toInteger(),
                        serverNamespace: "OrbitSaaS"
                )
        )
        PrototypeAuthorization onboardingScopeAuthorization = authorizationTypeRepository.saveAndFlush(
                new PrototypeAuthorization(name: "onboardingScopeAuthorization",
                        identities: [
                                verifiedPhoneOwner
                        ].toSet(),
                        scopes: [
                                onboardingScope
                        ].toSet(),
                        durationSeconds: Duration.ofMinutes(30).seconds.toInteger(),
                        serverNamespace: "OrbitSaaS",
                        refresh: refresh1dayNonRenewable,
                        prerequisites: [
                                legalAuthorization
                        ].toSet()
                )
        )
        PrototypeAuthorization adminOnboardingScopeAuthorization = authorizationTypeRepository.saveAndFlush(
                new PrototypeAuthorization(name: "adminOnboardingScopeAuthorization",
                        identities: [
                                verifiedPhoneOwner,
                                verifiedEmailOwner
                        ].toSet(),
                        scopes: [
                                adminOnboardingScope
                        ].toSet(),
                        durationSeconds: Duration.ofMinutes(30).seconds.toInteger(),
                        serverNamespace: "OrbitSaaS",
                        refresh: refresh1dayNonRenewable,
                        prerequisites: [
                                legalAuthorization
                        ].toSet()
                )
        )
        PrototypeAuthorization registeredUserScopeAuthorization = authorizationTypeRepository.saveAndFlush(
                new PrototypeAuthorization(name: "registeredUserScopeAuthorization",
                        identities: [
                                registeredUser
                        ].toSet(),
                        scopes: [
                                registeredUserScope
                        ].toSet(),
                        durationSeconds: Duration.ofMinutes(5).seconds.toInteger(),
                        serverNamespace: "OrbitSaaS",
                        prerequisites: [
                                onboardingScopeAuthorization
                        ].toSet(),
                        refresh: refresh30daysNonRenewable
                )
        )
        authorizationTypeRepository.saveAndFlush(
                new PrototypeAuthorization(name: "kycScopeAuthorization",
                        identities: [
                                knownCustomer
                        ].toSet(),
                        scopes: [
                                knownCustomerScope
                        ].toSet(),
                        durationSeconds: Duration.ofMinutes(5).seconds.toInteger(),
                        serverNamespace: "OrbitSaaS",
                        refresh: refresh30daysNonRenewable,
                        prerequisites: [
                                registeredUserScopeAuthorization
                        ].toSet()
                )
        )
        authorizationTypeRepository.saveAndFlush(
                new PrototypeAuthorization(name: "adminScopeAuthorization",
                        identities: [
                                admin
                        ].toSet(),
                        scopes: [
                                adminScope
                        ].toSet(),
                        durationSeconds: Duration.ofMinutes(5).seconds.toInteger(),
                        serverNamespace: "OrbitSaaS",
                        refresh: refresh1dayNonRenewable,
                        prerequisites: [
                                adminOnboardingScopeAuthorization
                        ].toSet()
                )
        )
        authorizationTypeRepository.flush()
    }

}