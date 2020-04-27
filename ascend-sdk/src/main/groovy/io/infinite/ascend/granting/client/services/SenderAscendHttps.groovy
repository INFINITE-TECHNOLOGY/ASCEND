package io.infinite.ascend.granting.client.services


import io.infinite.ascend.granting.common.other.AscendException
import io.infinite.ascend.validation.other.AscendForbiddenException
import io.infinite.ascend.validation.other.AscendUnauthorizedException
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import io.infinite.http.HttpRequest
import io.infinite.http.HttpResponse
import io.infinite.http.SenderDefaultHttps
import org.springframework.stereotype.Service

@BlackBox(level = CarburetorLevel.METHOD)
@Service
class SenderAscendHttps {

    SenderDefaultHttps senderDefaultHttps = new SenderDefaultHttps()

    HttpResponse sendAuthorizedHttpMessage(HttpRequest httpRequest) {
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
                throw new AscendException("Unexpected Ascend Granting Server HTTP status: " + httpResponse.toString())
                break
        }
    }

}
