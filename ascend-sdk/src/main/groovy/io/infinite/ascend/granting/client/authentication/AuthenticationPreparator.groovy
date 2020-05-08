package io.infinite.ascend.granting.client.authentication

import io.infinite.ascend.common.entities.AuthenticationData

interface AuthenticationPreparator {

    void prepareAuthentication(Map<String, String> publicCredentials, Map<String, String> privateCredentials)

}