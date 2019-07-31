package io.infinite.ascend.granting.model

import groovy.transform.ToString
import io.infinite.ascend.granting.model.enums.AuthenticationStatus

@ToString(includeNames = true, includeFields = true)
class Authentication {

    String name

    AuthenticationData authenticationData

    AuthenticationStatus status

}
