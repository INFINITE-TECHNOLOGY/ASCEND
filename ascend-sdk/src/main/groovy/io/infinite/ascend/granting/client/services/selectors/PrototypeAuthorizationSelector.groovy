package io.infinite.ascend.granting.client.services.selectors


import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization

interface PrototypeAuthorizationSelector {

    PrototypeAuthorization select(Set<PrototypeAuthorization> prototypeAuthorizations)

    PrototypeAuthorization selectPrerequisite(Set<PrototypeAuthorization> prototypeAuthorizations)

}