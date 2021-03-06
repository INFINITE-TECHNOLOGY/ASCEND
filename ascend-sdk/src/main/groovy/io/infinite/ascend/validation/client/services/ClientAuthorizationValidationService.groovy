package io.infinite.ascend.validation.client.services

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.common.exceptions.AscendException
import io.infinite.ascend.common.exceptions.AscendForbiddenException
import io.infinite.ascend.common.exceptions.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import io.infinite.http.HttpRequest
import io.infinite.http.HttpResponse
import io.infinite.http.SenderDefaultHttps
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
@Service
class ClientAuthorizationValidationService {

    ObjectMapper objectMapper = new ObjectMapper()

    Authorization validateClaim(String ascendValidationUrl, Claim claim) {
        HttpResponse httpResponse = new SenderDefaultHttps().sendHttpMessage(
                new HttpRequest(
                        url: "$ascendValidationUrl/ascend/public/validation",
                        headers: [
                                "Content-Type": "application/json",
                                "Accept"      : "application/json"
                        ],
                        method: "POST",
                        body: objectMapper.writeValueAsString(claim)
                )
        )
        switch (httpResponse.status) {
            case 200:
                return objectMapper.readValue(httpResponse.body, Authorization.class)
                break
            case 403:
                throw new AscendForbiddenException(httpResponse.body)
                break
            case 401:
                throw new AscendUnauthorizedException(httpResponse.body)
                break
            default:
                throw new AscendException("Unexpected Ascend Validation Server HTTP status: " + httpResponse.status)
                break
        }
    }

    void validateServletRequest(String ascendValidationUrl, HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            String authorizationHeader = request.getHeader("Authorization")
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response)
                return
            }
            String clientUrl
            if (request.getQueryString() != null) {
                clientUrl = request.requestURL
                        .append('?')
                        .append(request.getQueryString())
                        .toString()
            } else {
                clientUrl = request.requestURL
            }
            Claim claim = new Claim(
                    url: clientUrl,
                    jwt: authorizationHeader.replace("Bearer ", ""),
                    method: request.method
            )
            Authorization authorization = validateClaim(ascendValidationUrl, claim)
            PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken =
                    new PreAuthenticatedAuthenticationToken(authorization, claim)
            preAuthenticatedAuthenticationToken.setAuthenticated(true)
            SecurityContextHolder.getContext().setAuthentication(preAuthenticatedAuthenticationToken)
            chain.doFilter(request, response)
        }
        catch (AscendForbiddenException ascendForbiddenException) {
            SecurityContextHolder.clearContext()
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ascendForbiddenException.message, ascendForbiddenException)
        } catch (AscendUnauthorizedException ascendUnauthorizedException) {
            SecurityContextHolder.clearContext()
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ascendUnauthorizedException.message, ascendUnauthorizedException)
        } catch (Exception exception) {
            SecurityContextHolder.clearContext()
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Client exception during validation", exception)
        }
    }

}
