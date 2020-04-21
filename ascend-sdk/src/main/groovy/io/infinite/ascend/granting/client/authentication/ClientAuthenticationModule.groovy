package io.infinite.ascend.granting.client.authentication

import io.infinite.ascend.common.entities.AuthenticationData

interface ClientAuthenticationModule {

    AuthenticationData authenticate()

}