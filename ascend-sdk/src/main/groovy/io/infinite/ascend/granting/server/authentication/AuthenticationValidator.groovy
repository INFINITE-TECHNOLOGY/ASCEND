package io.infinite.ascend.granting.server.authentication

interface AuthenticationValidator {

    Map<String, String> validate(Map<String, String> publicCredentials, Map<String, String> privateCredentials)

}
