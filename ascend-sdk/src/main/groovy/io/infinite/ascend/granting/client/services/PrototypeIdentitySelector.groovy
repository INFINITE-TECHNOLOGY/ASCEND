package io.infinite.ascend.granting.client.services

import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity

interface PrototypeIdentitySelector {

    PrototypeIdentity select(Set<PrototypeIdentity> prototypeIdentities)

}