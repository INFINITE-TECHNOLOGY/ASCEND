package io.infinite.ascend.validation.client.services

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Claim
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.HttpRequest
import io.infinite.http.SenderDefaultHttps
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class ClientAuthorizationValidationService {

    ObjectMapper objectMapper = new ObjectMapper()

    Integer validateAscendHttpRequest(String ascendValidationUrl, String jwt, Claim claim) {
        return new SenderDefaultHttps().sendHttpMessage(new HttpRequest(
                url: ascendValidationUrl,
                headers: [
                        "Authorization": jwt,
                        "content-type" : "application/json"
                ],
                method: "POST",
                body: objectMapper.writeValueAsString(claim)
        )).status
    }

    void validateServletRequest(String ascendValidationUrl, HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            String authorizationHeader = request.getHeader("Authorization")
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response)
                return
            }
            String incomingUrl
            if (request.getQueryString() != null) {
                incomingUrl = request.requestURL
                        .append('?')
                        .append(request.getQueryString())
                        .toString()
            } else {
                incomingUrl = request.requestURL
            }
            Claim ascendHttpRequest = new Claim(
                    incomingUrl: incomingUrl,
                    method: request.method
            )
            Integer ascendHttpResponseStatus = validateAscendHttpRequest(ascendValidationUrl, authorizationHeader, ascendHttpRequest)
            if (ascendHttpResponseStatus != 200) {
                SecurityContextHolder.clearContext()
                response.sendError(ascendHttpResponseStatus, "Unauthorized.")
                return
            }
            PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken =
                    new PreAuthenticatedAuthenticationToken(ascendHttpRequest, authorizationHeader)
            preAuthenticatedAuthenticationToken.setAuthenticated(true)
            SecurityContextHolder.getContext().setAuthentication(preAuthenticatedAuthenticationToken)
            chain.doFilter(request, response)
        }
        catch (Exception e) {
            log.error("Exception", e)
            SecurityContextHolder.clearContext()
        }
    }

}
