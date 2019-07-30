package io.infinite.ascend.granting.model.enums

enum AuthorizationErrorCode {

    OTHER("OTHER"),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED")

    private final String stringValue

    AuthorizationErrorCode(String iStringValue) {
        stringValue = iStringValue
    }

    String stringValue() {
        return stringValue
    }

}