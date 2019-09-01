package io.infinite.ascend.validation.entities

import groovy.transform.ToString

import javax.persistence.*

@ToString(includeNames = true, includeFields = true)
class Usage {

    Long authorizationUsageId

    UUID authorizationId

    Date usageDate

}
