package io.infinite.ascend.granting.client.services

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.common.entities.Refresh
import io.infinite.ascend.common.exceptions.AscendException
import io.infinite.ascend.common.exceptions.AscendForbiddenException
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.common.repositories.RefreshRepository
import io.infinite.ascend.granting.client.authentication.AuthenticationPreparator
import io.infinite.ascend.granting.client.services.selectors.AuthorizationSelector
import io.infinite.ascend.granting.client.services.selectors.PrototypeAuthorizationSelector
import io.infinite.ascend.granting.client.services.selectors.PrototypeIdentitySelector
import io.infinite.ascend.granting.common.services.PrototypeConverter
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.HttpRequest
import io.infinite.http.HttpResponse
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
@CompileStatic
@BlackBox(level = CarburetorLevel.METHOD)
class ClientAuthorizationGrantingService {

    ObjectMapper objectMapper = new ObjectMapper()

    @Autowired
    AuthorizationRepository authorizationRepository

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

    HttpResponse sendAuthorizedHttpMessage(AuthorizedHttpRequest authorizedHttpRequest) {
        Authorization authorization = grantByScope(
                authorizedHttpRequest.scopeName,
                authorizedHttpRequest.ascendUrl,
                authorizedHttpRequest.authorizationClientNamespace,
                authorizedHttpRequest.authorizationServerNamespace
        )
        authorizedHttpRequest.headers.put("Authorization", "Bearer " + authorization.jwt)
        consume(authorization, new Claim(
                url: authorizedHttpRequest.url,
                method: authorizedHttpRequest.method,
                body: authorizedHttpRequest.body
        ))
        return sendHttpMessage(authorizedHttpRequest)
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
                throw new AscendException("Unexpected HTTP status: " + httpResponse.toString())
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
        Set<Authorization> existingAuthorizations = authorizationRepository.findAuthorization(clientNamespace, serverNamespace, prototypeAuthorization.name)
        if (!existingAuthorizations.empty) {
            return authorizationSelector.select(existingAuthorizations)
        } else {
            Set<Refresh> existingRefresh = refreshRepository.findRefresh(clientNamespace, serverNamespace, prototypeAuthorization.name)
            if (!existingRefresh.empty) {
                return sendRefresh(existingRefresh.first(), ascendUrl)
            } else {
                Authorization prerequisite = null
                if (!prototypeAuthorization.prerequisites.empty) {
                    PrototypeAuthorization prototypeAuthorizationPrerequisite = prototypeAuthorizationSelector.selectPrerequisite(prototypeAuthorization.prerequisites)
                    prerequisite = grantByPrototype(prototypeAuthorizationPrerequisite, ascendUrl, clientNamespace, serverNamespace)//<<<Recursive call here
                }
                Authorization authorization = clientAuthentication(prototypeAuthorization, clientNamespace, prototypeIdentity)
                authorization.prerequisite = prerequisite
                return sendAuthorization(authorization, ascendUrl)
            }
        }
    }

    Authorization clientAuthentication(PrototypeAuthorization prototypeAuthorization, String clientNamespace, PrototypeIdentity prototypeIdentity) {
        Authorization authorization = prototypeConverter.convertAccessAuthorization(prototypeAuthorization, clientNamespace)
        authorization.scope = prototypeConverter.convertScope(prototypeAuthorization.scopes.first())
        authorization.identity = prototypeConverter.convertIdentity(prototypeIdentity)
        authorization.identity.authentications.each { authentication ->
            prepareAuthentication(authentication.name, authorization.identity.publicCredentials, authentication.privateCredentials)
        }
        return authorization
    }

    void prepareAuthentication(String authenticationName, Map<String, String> publicCredentials, Map<String, String> privateCredentials) {
        AuthenticationPreparator authenticationPreparator
        try {
            authenticationPreparator = applicationContext.getBean(authenticationName + "Preparator", AuthenticationPreparator.class)
        } catch (NoSuchBeanDefinitionException noSuchBeanDefinitionException) {
            throw new AscendUnauthorizedException("Authentication Preparator not found: ${authenticationName + "Preparator"}", noSuchBeanDefinitionException)
        }
        authenticationPreparator.prepareAuthentication(publicCredentials, privateCredentials)
    }

    void consume(Authorization authorization, Claim claim) {
        authorization.claims.add(claim)
        authorizationRepository.saveAndFlush(authorization)
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

}
