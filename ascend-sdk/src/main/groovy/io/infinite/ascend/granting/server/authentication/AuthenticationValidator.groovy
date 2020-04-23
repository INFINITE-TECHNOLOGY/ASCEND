package io.infinite.ascend.granting.server.authentication

import io.infinite.ascend.common.entities.Authentication
import io.infinite.ascend.common.entities.Authorization

interface AuthenticationValidator {

    Map<String, String> authenticate(Authentication authentication, Authorization authorization)

}
