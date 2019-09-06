package io.infinite.ascend.validation.model

import groovy.transform.ToString
import io.infinite.ascend.granting.model.Authorization

@ToString(includeNames = true, includeFields = true)
class AscendHttpRequest {

    String authorizationHeader
    String incomingUrl
    String method
    String body
    Integer status
    String statusDescription
    Authorization authorization

}
