package io.infinite.ascend.granting.client.services.selectors

import io.infinite.ascend.granting.configuration.entities.PrototypeIdentity

interface PrototypeIdentitySelector {

    PrototypeIdentity select(Set<PrototypeIdentity> prototypeIdentities)

    PrototypeIdentity selectPrerequisite(Set<PrototypeIdentity> prototypeIdentities)

}