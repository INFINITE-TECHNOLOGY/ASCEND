package io.infinite.ascend.config.entities.enums

enum HttpMethod {

    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE");

    private final String stringValue

    HttpMethod(String iStringValue) {
        stringValue = iStringValue
    }

    String stringValue() {
        return stringValue
    }

}