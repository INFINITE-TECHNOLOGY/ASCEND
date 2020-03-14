package io.infinite.ascend.granting.model

import com.fasterxml.jackson.annotation.JsonFormat
import groovy.transform.ToString
import io.infinite.ascend.granting.model.enums.AuthorizationErrorCode
import io.infinite.ascend.granting.model.enums.AuthorizationPurpose
import io.infinite.ascend.granting.model.enums.AuthorizationStatus

@ToString(includeNames = true, includeFields = true)
class Authorization {

    UUID id = UUID.randomUUID()

    String name

    Identity identity

    Scope scope

    Integer durationSeconds

    Integer maxUsageCount

    Authorization refreshAuthorization

    @JsonFormat(timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    Date creationDate

    @JsonFormat(timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    Date expiryDate

    AuthorizationStatus status

    AuthorizationErrorCode errorCode

    String jwt

    String prerequisiteJwt

    AuthorizationPurpose purpose

}
