package io.infinite.ascend.granting.client.authentication

interface AuthenticationPreparator {

    void prepareAuthentication(Map<String, String> publicCredentials, Map<String, String> privateCredentials, Optional<String> prerequisiteJwt)

}