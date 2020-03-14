package io.infinite.ascend.granting.model

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class Grant {

    String bodyRegex

    String urlRegex

    String httpMethod

}
