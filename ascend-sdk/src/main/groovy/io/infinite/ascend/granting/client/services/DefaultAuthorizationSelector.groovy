package io.infinite.ascend.granting.client.services

import io.infinite.ascend.common.entities.Authorization
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class DefaultAuthorizationSelector implements AuthorizationSelector {

    @Override
    Authorization select(Set<Authorization> authorizations) {
        return authorizations.first()
    }

}
