package io.infinite.ascend.granting.model.enums

enum AuthenticationStatus {

    NEW("NEW"),
    FAILED("FAILED"),
    SUCCESSFUL("SUCCESSFUL")

    private final String stringValue

    AuthenticationStatus(String iStringValue) {
        stringValue = iStringValue
    }

    String stringValue() {
        return stringValue
    }

}