package io.infinite.ascend.granting.common.other

class AscendException extends Exception {

    AscendException(String message) {
        super(message)
    }

    AscendException(String message, Throwable throwable) {
        super(message, throwable)
    }

}