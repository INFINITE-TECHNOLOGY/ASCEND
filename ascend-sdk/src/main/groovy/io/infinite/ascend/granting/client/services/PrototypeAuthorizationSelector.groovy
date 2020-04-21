package io.infinite.ascend.granting.client.services


import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization

interface PrototypeAuthorizationSelector {

    PrototypeAuthorization select(Set<PrototypeAuthorization> prototypeAuthorizations)

}