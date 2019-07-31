package io.infinite.ascend.granting.model

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class Identity {

    String name

    Set<Authentication> authentications = new HashSet<Authentication>()

}
