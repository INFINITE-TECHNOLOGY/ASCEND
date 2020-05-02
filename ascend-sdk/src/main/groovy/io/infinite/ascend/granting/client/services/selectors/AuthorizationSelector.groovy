package io.infinite.ascend.granting.client.services.selectors

import io.infinite.ascend.common.entities.Authorization

interface AuthorizationSelector {

    Authorization select(Set<Authorization> Authorizations)

    Authorization selectPrerequisite(Set<Authorization> Authorizations)

}