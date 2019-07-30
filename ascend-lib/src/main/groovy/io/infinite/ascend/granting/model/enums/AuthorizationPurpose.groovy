package io.infinite.ascend.granting.model.enums

enum AuthorizationPurpose {

    ACCESS("ACCESS"),
    REFRESH("REFRESH")

    private final String stringValue

    AuthorizationPurpose(String iStringValue) {
        stringValue = iStringValue
    }

    String stringValue() {
        return stringValue
    }

}