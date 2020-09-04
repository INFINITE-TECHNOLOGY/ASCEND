package io.infinite.ascend.granting.server.authentication

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.springframework.stereotype.Service

@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
@Service
class EmailOtpValidator extends SmsOtpValidator {

    @Override
    Map<String, String> authorizeCredentials(Map<String, String> publicCredentials) {
        return ["email": publicCredentials.get("email")]
    }

}
