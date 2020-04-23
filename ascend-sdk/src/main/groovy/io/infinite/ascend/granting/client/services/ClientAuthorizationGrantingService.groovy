package io.infinite.ascend.granting.client.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.common.repositories.AuthorizationRepository
import io.infinite.ascend.granting.common.other.AscendException
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity
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
    ClientAuthenticationService clientAuthenticationService

    @Autowired
    AuthorizationSelector authorizationSelector

    @Transactional
    Authorization scopedAuthorization(String scopeName, String ascendUrl, String authorizationClientNamespace, String authorizationServerNamespace) {
        Authorization authorization
        Set<PrototypeAuthorization> prototypeAuthorizations = inquire(scopeName, ascendUrl, authorizationServerNamespace)
        if (prototypeAuthorizations.isEmpty()) {
            throw new AscendException("No suitable authorizations found for scope name '$scopeName' with authorization serverNamespace '$authorizationServerNamespace' (Ascend URL $ascendUrl)")
        }
        PrototypeAuthorization prototypeAuthorization = prototypeAuthorizationSelector.select(prototypeAuthorizations)
        PrototypeIdentity prototypeIdentity = prototypeIdentitySelector.select(prototypeAuthorization.identities)
        authorization = produce(prototypeAuthorization, authorizationClientNamespace, authorizationServerNamespace, prototypeIdentity, ascendUrl)
        return authorization
    }

    Authorization produce(PrototypeAuthorization prototypeAuthorization, String authorizationClientNamespace, String authorizationServerNamespace, PrototypeIdentity prototypeIdentity, String ascendUrl) {
        Authorization authorization
        Set<Authorization> existingAuthorizations = authorizationRepository.findReceived(authorizationClientNamespace, authorizationServerNamespace, prototypeAuthorization.name)
        if (!existingAuthorizations.isEmpty()) {
            authorization = authorizationSelector.select(existingAuthorizations)
        } else {
            authorization = prototypeConverter.convertAuthorization(prototypeAuthorization, authorizationServerNamespace)
            if (!prototypeAuthorization.prerequisites.empty) {
                PrototypeAuthorization prototypeAuthorizationPrerequisite = prototypeAuthorizationSelector.selectPrerequisite(prototypeAuthorization.prerequisites)
                PrototypeIdentity prototypeIdentityPrerequisite = prototypeIdentitySelector.selectPrerequisite(prototypeAuthorizationPrerequisite.identities)
                authorization.prerequisiteJwt = produce(prototypeAuthorizationPrerequisite, authorizationClientNamespace, authorizationServerNamespace, prototypeIdentityPrerequisite, ascendUrl).jwt
                //<<<<<<<<Recursive call
            }
            authorization.scope = prototypeConverter.convertScope(prototypeAuthorization.scopes.first())
            authorization.scope.grants = []
            prototypeAuthorization.scopes.first().grants.each { prototypeGrant ->
                authorization.scope.grants.add(prototypeConverter.convertGrant(prototypeGrant))
            }
            authorization.identity = prototypeConverter.convertIdentity(prototypeIdentity)
            authorization.identity.authentications = []
            prototypeIdentity.authentications.each { prototypeAuthentication ->
                Authentication authentication = prototypeConverter.convertAuthentication(prototypeAuthentication)
                clientAuthenticationService.authenticate(authentication)
                authorization.identity.authentications.add(authentication)
            }
            authorization = sendToAuthorizationServer(authorization, ascendUrl)
            if (!authorization.isSuccessful) {
                throw new AscendException("Authorization failed: $authorization")
            }
        }
        authorizationRepository.saveAndFlush(authorization)
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

    Authorization sendToAuthorizationServer(Authorization authorization, String ascendUrl) {
        return objectMapper.readValue(
                new SenderDefaultHttps().expectStatus(
                        new HttpRequest(
                                url: "$ascendUrl/ascend/public/granting",
                                headers: ["Content-Type": "application/json;charset=UTF-8"],
                                method: "POST",
                                body: objectMapper.writeValueAsString(authorization)
                        ), 200
                ).body, Authorization.class)
    }

}