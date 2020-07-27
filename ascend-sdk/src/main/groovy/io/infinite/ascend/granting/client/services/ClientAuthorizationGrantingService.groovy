package io.infinite.ascend.granting.client.services

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.common.entities.Refresh
import io.infinite.ascend.common.exceptions.AscendException
import io.infinite.ascend.common.exceptions.AscendForbiddenException
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.common.repositories.ClaimRepository
import io.infinite.ascend.common.repositories.RefreshRepository
import io.infinite.ascend.granting.client.authentication.AuthenticationPreparator
import io.infinite.ascend.granting.client.services.selectors.AuthorizationSelector
import io.infinite.ascend.granting.client.services.selectors.PrototypeAuthorizationSelector
import io.infinite.ascend.granting.client.services.selectors.PrototypeIdentitySelector
import io.infinite.ascend.granting.common.services.PrototypeConverter
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.http.HttpException
import io.infinite.http.HttpRequest
import io.infinite.http.HttpResponse
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

import java.time.Duration
import java.time.Instant

@Service
@CompileStatic
@BlackBox(level = BlackBoxLevel.METHOD)
class ClientAuthorizationGrantingService {

    ObjectMapper objectMapper = new ObjectMapper()

    @Autowired
    AuthorizationRepository authorizationRepository

    @Autowired
    ClaimRepository claimRepository

    @Autowired
    RefreshRepository refreshRepository

    @Autowired
    PrototypeAuthorizationSelector prototypeAuthorizationSelector

    @Autowired
    PrototypeIdentitySelector prototypeIdentitySelector

    @Autowired
    PrototypeConverter prototypeConverter

    @Autowired
    AuthorizationSelector authorizationSelector

    @Autowired
    ApplicationContext applicationContext

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    @Value('${refreshBufferSeconds:10}')
    Integer refreshBufferSeconds

    @Value('${accessBufferSeconds:5}')
    Integer accessBufferSeconds

    HttpResponse sendAuthorizedHttpMessage(AuthorizedHttpRequest authorizedHttpRequest) {
        Authorization authorization = grantByScope(
                authorizedHttpRequest.scopeName,
                authorizedHttpRequest.ascendUrl,
                authorizedHttpRequest.authorizationClientNamespace,
                authorizedHttpRequest.authorizationServerNamespace
        )
        authorizedHttpRequest.headers.put("Authorization", "Bearer " + authorization.jwt)
        Claim claim = new Claim(
                url: authorizedHttpRequest.url,
                method: authorizedHttpRequest.method,
                body: authorizedHttpRequest.body
        )
        authorization.claims.add(claimRepository.save(claim))
        authorizationRepository.saveAndFlush(authorization)
        return sendHttpMessage(authorizedHttpRequest)
    }

    HttpResponse expectAuthorizedStatus(AuthorizedHttpRequest authorizedHttpRequest, Integer expectedStatus) {
        HttpResponse httpResponse = sendAuthorizedHttpMessage(authorizedHttpRequest)
        if (httpResponse.status != expectedStatus) {
            throw new HttpException("Failed HTTP Response code: ${httpResponse.status}")
        }
        return httpResponse
    }

    HttpResponse sendHttpMessage(HttpRequest httpRequest) {
        HttpResponse httpResponse = senderDefaultHttps.sendHttpMessage(httpRequest)
        switch (httpResponse.status) {
            case 200:
                return httpResponse
                break
            case 403:
                throw new AscendForbiddenException(httpResponse.body)
                break
            case 401:
                throw new AscendUnauthorizedException(httpResponse.body)
                break
            default:
                throw new AscendException("Unexpected HTTP status: " + httpResponse.status)
                break
        }
    }

    Authorization grantByScope(String scopeName, String ascendUrl, String clientNamespace, String serverNamespace) {
        Set<PrototypeAuthorization> prototypeAuthorizations = inquire(scopeName, ascendUrl, serverNamespace)
        if (prototypeAuthorizations.empty) {
            throw new AscendUnauthorizedException("No suitable prototype authorizations found for scope name '$scopeName' with serverNamespace '$serverNamespace' (Ascend URL $ascendUrl)")
        } else {
            return grantByPrototype(prototypeAuthorizationSelector.select(prototypeAuthorizations), ascendUrl, clientNamespace, serverNamespace)
        }
    }

    Authorization grantByPrototype(PrototypeAuthorization prototypeAuthorization, String ascendUrl, String clientNamespace, String serverNamespace) {
        PrototypeIdentity prototypeIdentity = prototypeIdentitySelector.select(prototypeAuthorization.identities)
        Set<Authorization> existingAuthorizations = authorizationRepository.findAuthorization(clientNamespace, serverNamespace, prototypeAuthorization.name, cutoff(accessBufferSeconds))
        if (!existingAuthorizations.empty) {
            return authorizationSelector.select(existingAuthorizations)
        } else {
            Set<Refresh> existingRefresh = refreshRepository.findRefresh(clientNamespace, serverNamespace, prototypeAuthorization.name, cutoff(refreshBufferSeconds))
            if (!existingRefresh.empty) {
                return sendRefresh(existingRefresh.first(), ascendUrl)
            } else {
                Authorization authorization = prototypeConverter.convertAccessAuthorization(prototypeAuthorization, clientNamespace)
                authorization.scope = prototypeConverter.convertScope(prototypeAuthorization.scopes.first())
                authorization.identity = prototypeConverter.convertIdentity(prototypeIdentity)
                if (!prototypeAuthorization.prerequisites.empty) {
                    PrototypeAuthorization prototypeAuthorizationPrerequisite = prototypeAuthorizationSelector.selectPrerequisite(prototypeAuthorization.prerequisites)
                    //Recursive call here
                    authorization.prerequisite = grantByPrototype(prototypeAuthorizationPrerequisite, ascendUrl, clientNamespace, serverNamespace)
                    authorization.identity.publicCredentials = authorization.prerequisite.authorizedCredentials
                }
                authorization.identity.authentications.each { authentication ->
                    prepareAuthentication(authentication.name, authorization.identity.publicCredentials, authentication.privateCredentials, Optional.ofNullable(authorization.prerequisite?.jwt))
                }
                return sendAuthorization(authorization, ascendUrl)
            }
        }
    }

    void prepareAuthentication(String authenticationName, Map<String, String> publicCredentials, Map<String, String> privateCredentials, Optional<String> prerequisiteJwt) {
        AuthenticationPreparator authenticationPreparator
        try {
            authenticationPreparator = applicationContext.getBean(authenticationName + "Preparator", AuthenticationPreparator.class)
        } catch (NoSuchBeanDefinitionException ignored) {
            throw new AscendUnauthorizedException("Authentication Preparator not found: ${authenticationName + "Preparator"}")
        }
        authenticationPreparator.prepareAuthentication(publicCredentials, privateCredentials)
    }

    Set<PrototypeAuthorization> inquire(String scopeName, String ascendGrantingUrl, String authorizationServerNamespace) {
        return objectMapper.readValue(
                sendHttpMessage(
                        new HttpRequest(
                                url: "$ascendGrantingUrl/ascend/public/granting/inquire?scopeName=${scopeName}&serverNamespace=${authorizationServerNamespace}",
                                headers: [
                                        "Content-Type": "application/json",
                                        "Accept"      : "application/json"
                                ],
                                method: "GET"
                        )
                ).body, PrototypeAuthorization[].class) as Set<PrototypeAuthorization>
    }

    Authorization sendRefresh(Refresh refresh, String ascendGrantingUrl) {
        return authorizationRepository.saveAndFlush(objectMapper.readValue(
                sendHttpMessage(
                        new HttpRequest(
                                url: "$ascendGrantingUrl/ascend/public/granting/refresh",
                                headers: [
                                        "Content-Type": "application/json;charset=UTF-8",
                                        "Accept"      : "application/json"
                                ],
                                method: "POST",
                                body: refresh.jwt
                        )
                ).body, Authorization.class))
    }

    Authorization sendAuthorization(Authorization authorization, String ascendGrantingUrl) {
        return authorizationRepository.saveAndFlush(objectMapper.readValue(
                sendHttpMessage(
                        new HttpRequest(
                                url: "$ascendGrantingUrl/ascend/public/granting/access",
                                headers: [
                                        "Content-Type": "application/json;charset=UTF-8",
                                        "Accept"      : "application/json"
                                ],
                                method: "POST",
                                body: objectMapper.writeValueAsString(authorization)
                        )
                ).body, Authorization.class))
    }

    Date cutoff(Integer bufferSeconds) {
        return (Instant.now() + Duration.ofSeconds(bufferSeconds)).toDate()
    }

}
