package io.infinite.ascend.validation.client.services

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.common.entities.Claim
import io.infinite.ascend.granting.common.other.AscendException
import io.infinite.ascend.validation.other.AscendForbiddenException
import io.infinite.ascend.validation.other.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.HttpRequest
import io.infinite.http.HttpResponse
import io.infinite.http.SenderDefaultHttps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
@Service
class ClientAuthorizationValidationService {

    ObjectMapper objectMapper = new ObjectMapper()

    @Autowired
    String ascendValidationUrl

    Authorization authorizeClaim(Claim claim) {
        HttpResponse httpResponse = new SenderDefaultHttps().sendHttpMessage(
                new HttpRequest(
                        url: ascendValidationUrl,
                        headers: [
                                "content-type": "application/json"
                        ],
                        method: "POST",
                        body: objectMapper.writeValueAsString(claim)
                )
        )
        switch (httpResponse.status) {
            case 200:
                return objectMapper.readValue(httpResponse.body , Authorization.class)
                break
            case 403:
                throw new AscendForbiddenException(httpResponse.body)
                break
            case 401:
                throw new AscendUnauthorizedException(httpResponse.body)
                break
            default:
                throw new AscendException("Unexpected Ascend Validation Server HTTP status: " + httpResponse.toString())
                break
        }
    }

    void validateServletRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
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
            Claim claim = new Claim(
                    url: incomingUrl,
                    jwt: authorizationHeader.replace("Bearer ", ""),
                    method: request.method
            )
            Authorization authorization = authorizeClaim(claim)
            if (!authorization.isSuccessful) {
                log.warn("Inconsistent server validation results: 200 response code, but Authorization not successful.")
                throw new AscendUnauthorizedException("Unauthorized")
            }
            PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken =
                    new PreAuthenticatedAuthenticationToken(claim, authorizationHeader)
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
