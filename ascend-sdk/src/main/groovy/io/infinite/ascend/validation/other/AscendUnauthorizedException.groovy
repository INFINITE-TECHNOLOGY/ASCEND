package io.infinite.ascend.validation.other

class AscendUnauthorizedException extends Exception {

    AscendUnauthorizedException(String message) {
        super(message)
    }

    AscendUnauthorizedException(String message, Throwable throwable) {
        super(message, throwable)
    }

}