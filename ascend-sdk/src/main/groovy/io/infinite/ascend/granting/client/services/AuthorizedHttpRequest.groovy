package io.infinite.ascend.granting.client.services

import groovy.transform.ToString
import io.infinite.http.HttpRequest

@ToString(includeNames = true, includeFields = true, includeSuper = true)
class AuthorizedHttpRequest extends HttpRequest {

    String scopeName

    String ascendUrl

    String authorizationClientNamespace

    String authorizationServerNamespace

}
