package io.infinite.ascend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.infinite.ascend.client.App2AppAuthorizationHelper
import io.infinite.ascend.granting.model.Authorization

class AscendHelper {

    static void main(String[] args) {
        Authorization orbitAuthorization = new App2AppAuthorizationHelper().createApp2AppAuthorization(System.getenv("ASCEND_APP_NAME"), "ASCEND_GFS", "Orbit")
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(System.out, orbitAuthorization)
    }

}
