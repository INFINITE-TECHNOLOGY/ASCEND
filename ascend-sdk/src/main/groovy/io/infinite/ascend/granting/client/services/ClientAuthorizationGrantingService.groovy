package io.infinite.ascend.granting.client.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.granting.common.other.AscendException
import io.infinite.ascend.common.repositories.AuthorizationRepository
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
    Authorization clientAuthorization(String scopeName, String ascendUrl, String authorizationNamespace) {
        Authorization authorization
        Set<PrototypeAuthorization> prototypeAuthorizations = inquire(scopeName, ascendUrl, authorizationNamespace)
        if (prototypeAuthorizations.isEmpty()) {
            throw new AscendException("No suitable authorizations found for scope name '$scopeName' with authorization namespace '$authorizationNamespace' (Ascend URL $ascendUrl)")
        }
        PrototypeAuthorization prototypeAuthorization = prototypeAuthorizationSelector.select(prototypeAuthorizations)
        PrototypeIdentity prototypeIdentity = prototypeIdentitySelector.select(prototypeAuthorization.identities)
        Set<Authorization> existingAuthorizations = authorizationRepository.findValidForClient(authorizationNamespace, prototypeAuthorization.name)
        if (!existingAuthorizations.isEmpty()) {
            authorization = authorizationSelector.select(existingAuthorizations)
        } else {
            authorization = prototypeConverter.convertAuthorization(prototypeAuthorization)
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
        consume(authorization)
        return authorization
    }

    void consume(Authorization authorization) {
        authorization.claims.add(new Claim())
        authorizationRepository.saveAndFlush(authorization)
    }

    Set<PrototypeAuthorization> inquire(String scopeName, String ascendUrl, String authorizationNamespace) {
        return objectMapper.readValue(
                new SenderDefaultHttps().expectStatus(
                        new HttpRequest(
                                url: "$ascendUrl/ascend/public/granting/inquire?scopeName=${scopeName}&namespace=${authorizationNamespace}",
                                method: "GET"
                        ), 200
                ).body, PrototypeAuthorization[].class)
    }

    Authorization sendToAuthorizationServer(Authorization authorization, String ascendUrl) {
        return objectMapper.readValue(
                new SenderDefaultHttps().expectStatus(
                        new HttpRequest(
                                url: "$ascendUrl/ascend/public/granting",
                                method: "POST",
                                body: objectMapper.writeValueAsString(authorization)
                        ), 200
                ).body, Authorization.class)
    }

}
