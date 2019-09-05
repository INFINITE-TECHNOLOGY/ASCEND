package io.infinite.ascend.validation.model

import io.infinite.ascend.granting.model.Authorization

class AscendHttpRequest {

    String authorizationHeader
    String incomingUrl
    String method
    String body
    Integer status
    String statusDescription
    Authorization authorization

}
