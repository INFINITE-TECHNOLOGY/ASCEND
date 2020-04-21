package io.infinite.ascend.common.model

import groovy.transform.ToString

import javax.persistence.Entity

@ToString(includeNames = true, includeFields = true, excludes = ["privateKey"])
class AscendKeyPair {

    String privateKey

    String publicKey

}
