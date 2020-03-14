package io.infinite.ascend.granting.model

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class Scope {

    String name

    Set<Grant> grants = new HashSet<Grant>()

}
