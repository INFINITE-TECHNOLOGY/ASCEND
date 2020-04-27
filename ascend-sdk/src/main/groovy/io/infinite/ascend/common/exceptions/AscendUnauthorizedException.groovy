package io.infinite.ascend.common.exceptions

import org.springframework.core.NestedRuntimeException

class AscendUnauthorizedException extends NestedRuntimeException {

    AscendUnauthorizedException(String message) {
        super(message)
    }

    AscendUnauthorizedException(String message, Throwable throwable) {
        super(message, throwable)
    }

}