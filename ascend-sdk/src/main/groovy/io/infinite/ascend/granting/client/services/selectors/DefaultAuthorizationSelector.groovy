package io.infinite.ascend.granting.client.services.selectors

import io.infinite.ascend.common.entities.Authorization
import org.springframework.stereotype.Service

@Service
class DefaultAuthorizationSelector implements AuthorizationSelector {

    @Override
    Authorization select(Set<Authorization> authorizations) {
        return authorizations.first()
    }

    @Override
    Authorization selectPrerequisite(Set<Authorization> authorizations) {
        return authorizations.first()
    }

}
