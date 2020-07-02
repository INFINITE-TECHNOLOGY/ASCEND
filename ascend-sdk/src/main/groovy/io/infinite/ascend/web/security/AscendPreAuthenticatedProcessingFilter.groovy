package io.infinite.ascend.web.security

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

import javax.servlet.http.HttpServletRequest

@BlackBox(level = BlackBoxLevel.METHOD)
class AscendPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return "No Principal"
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "No Credentials"
    }

}
