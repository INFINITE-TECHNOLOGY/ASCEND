package io.infinite.ascend.granting.client.services

import io.infinite.ascend.common.entities.Authorization

interface AuthorizationSelector {

    Authorization select(Set<Authorization> Authorizations)

}