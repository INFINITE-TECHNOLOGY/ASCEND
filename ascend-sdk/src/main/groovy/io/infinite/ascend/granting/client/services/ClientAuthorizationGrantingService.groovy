package io.infinite.ascend.granting.client.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.granting.client.services.selectors.AuthorizationSelector
import io.infinite.ascend.granting.client.services.selectors.PrototypeAuthorizationSelector
import io.infinite.ascend.granting.client.services.selectors.PrototypeIdentitySelector
import io.infinite.ascend.granting.common.services.PrototypeConverter
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity
import io.infinite.ascend.validation.other.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.HttpRequest
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Service
@BlackBox(level = CarburetorLevel.METHOD)
class ClientAuthorizationGrantingService {

    ObjectMapper objectMapper = new ObjectMapper()

    @Autowired
    AuthorizationRepository authorizationRepository

    @Autowired
    PrototypeAuthorizationSelector prototypeAuthorizationSelector

    @Autowired
    PrototypeIdentitySelector prototypeIdentitySelector

    @Autowired
    PrototypeConverter prototypeConverter

    @Autowired
    ClientAuthenticationPreparationService clientAuthenticationService

    @Autowired
    AuthorizationSelector authorizationSelector

    @Transactional
    Authorization grantByScope(String scopeName, String ascendUrl, String authorizationClientNamespace, String authorizationServerNamespace) {
        Authorization authorization
        Set<PrototypeAuthorization> prototypeAuthorizations = inquire(scopeName, ascendUrl, authorizationServerNamespace)
        if (prototypeAuthorizations.isEmpty()) {
            throw new AscendUnauthorizedException("No suitable authorizations found for scope name '$scopeName' with authorization serverNamespace '$authorizationServerNamespace' (Ascend URL $ascendUrl)")
        }
        PrototypeAuthorization prototypeAuthorization = prototypeAuthorizationSelector.select(prototypeAuthorizations)
        PrototypeIdentity prototypeIdentity = prototypeIdentitySelector.select(prototypeAuthorization.identities)
        authorization = clientAccessGranting(prototypeAuthorization, authorizationClientNamespace, authorizationServerNamespace, prototypeIdentity, ascendUrl)
        return authorization
    }

    Authorization clientAccessGranting(PrototypeAuthorization prototypeAuthorization, String authorizationClientNamespace, String authorizationServerNamespace, PrototypeIdentity prototypeIdentity, String ascendUrl) {
        Authorization authorization
        Set<Authorization> existingAuthorizations = authorizationRepository.findReceivedAccess(authorizationClientNamespace, authorizationServerNamespace, prototypeAuthorization.name)
        if (!existingAuthorizations.isEmpty()) {
            authorization = authorizationSelector.select(existingAuthorizations)
        } else {
            Set<Authorization> existingRefreshAuthorizations = authorizationRepository.findRefreshByAccess(authorizationClientNamespace, authorizationServerNamespace, prototypeAuthorization.name)
            if (!existingRefreshAuthorizations.isEmpty()) {
                authorization = serverRefreshGranting(existingRefreshAuthorizations.first(), ascendUrl)
            } else {
                authorization = prototypeConverter.convertAuthorization(prototypeAuthorization, authorizationClientNamespace)
                if (!prototypeAuthorization.prerequisites.empty) {
                    PrototypeAuthorization prototypeAuthorizationPrerequisite = prototypeAuthorizationSelector.selectPrerequisite(prototypeAuthorization.prerequisites)
                    PrototypeIdentity prototypeIdentityPrerequisite = prototypeIdentitySelector.selectPrerequisite(prototypeAuthorizationPrerequisite.identities)
                    authorization.prerequisite = clientAccessGranting(prototypeAuthorizationPrerequisite, authorizationClientNamespace, authorizationServerNamespace, prototypeIdentityPrerequisite, ascendUrl)
                    //<<<<<<<<Recursive call
                }
                authorization.scope = prototypeConverter.convertScope(prototypeAuthorization.scopes.first())
                authorization.identity = prototypeConverter.convertIdentity(prototypeIdentity)
                authorization.identity.authentications.each { prototypeAuthentication ->
                    clientAuthenticationService.prepareAuthentication(prototypeAuthentication)
                }
                authorization = serverAccessGranting(authorization, ascendUrl)
            }
        }
        return authorization
    }

    //todo: securedHttpRequest(...)

    void consume(Authorization authorization, Claim claim) {
        authorization.claims.add(claim)
        authorizationRepository.saveAndFlush(authorization)
    }

    Set<PrototypeAuthorization> inquire(String scopeName, String ascendUrl, String authorizationServerNamespace) {
        return objectMapper.readValue(
                new SenderDefaultHttps().expectStatus(
                        new HttpRequest(
                                url: "$ascendUrl/ascend/public/granting/inquire?scopeName=${scopeName}&serverNamespace=${authorizationServerNamespace}",
                                method: "GET"
                        ), 200
                ).body, PrototypeAuthorization[].class) as Set<PrototypeAuthorization>
    }

    Authorization serverRefreshGranting(Authorization refreshAuthorization, String ascendUrl) {
        return authorizationRepository.saveAndFlush(objectMapper.readValue(
                new SenderDefaultHttps().expectStatus(
                        new HttpRequest(
                                url: "$ascendUrl/ascend/public/granting/refresh",
                                headers: ["Content-Type": "application/json;charset=UTF-8"],
                                method: "POST",
                                body: objectMapper.writeValueAsString(refreshAuthorization)
                        ), 200
                ).body, Authorization.class))
    }

    Authorization serverAccessGranting(Authorization authorization, String ascendUrl) {
        return authorizationRepository.saveAndFlush(objectMapper.readValue(
                new SenderDefaultHttps().expectStatus(
                        new HttpRequest(
                                url: "$ascendUrl/ascend/public/granting/access",
                                headers: ["Content-Type": "application/json;charset=UTF-8"],
                                method: "POST",
                                body: objectMapper.writeValueAsString(authorization)
                        ), 200
                ).body, Authorization.class))
    }

}
