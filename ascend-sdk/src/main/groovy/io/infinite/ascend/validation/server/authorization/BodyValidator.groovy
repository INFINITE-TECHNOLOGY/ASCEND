package io.infinite.ascend.validation.server.authorization

interface BodyValidator {

    void validate(Map<String, String> authorizedCredentials, String body)

}
