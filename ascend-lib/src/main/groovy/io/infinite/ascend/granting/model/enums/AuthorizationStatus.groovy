package io.infinite.ascend.granting.model.enums

enum AuthorizationStatus {

    NEW("NEW"),
    FAILED("FAILED"),
    SUCCESSFUL("SUCCESSFUL")

    private final String stringValue

    AuthorizationStatus(String iStringValue) {
        stringValue = iStringValue
    }

    String stringValue() {
        return stringValue
    }

}