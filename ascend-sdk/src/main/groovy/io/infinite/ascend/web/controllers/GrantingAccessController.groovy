package io.infinite.ascend.web.controllers

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.common.entities.Authorization
import io.infinite.ascend.granting.server.services.ServerAuthorizationGrantingService
import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@BlackBox(level = BlackBoxLevel.METHOD)
@Slf4j
class GrantingAccessController {

    @Autowired
    ServerAuthorizationGrantingService serverAuthorizationGrantingService

    @PostMapping(value = "/public/granting/access")
    @ResponseBody
    @CompileDynamic
    @BlackBox(level = BlackBoxLevel.METHOD)
    @CrossOrigin
    Authorization postAuthorization(@RequestBody Authorization authorization) {
        return serverAuthorizationGrantingService.grantAccessAuthorization(authorization)
    }

}
