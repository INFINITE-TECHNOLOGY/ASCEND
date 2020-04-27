package io.infinite.ascend.common.exceptions

import org.springframework.core.NestedRuntimeException

class AscendException extends NestedRuntimeException {

    AscendException(String message) {
        super(message)
    }

    AscendException(String message, Throwable throwable) {
        super(message, throwable)
    }

}