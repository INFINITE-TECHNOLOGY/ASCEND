package io.infinite.ascend.common.exceptions

import org.springframework.core.NestedRuntimeException

class AscendForbiddenException extends NestedRuntimeException {

    AscendForbiddenException(String message) {
        super(message)
    }

}