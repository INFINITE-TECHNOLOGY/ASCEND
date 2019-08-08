package io.infinite.ascend.granting.controllers


import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.infinite.ascend.granting.AuthorizationGranting
import io.infinite.ascend.granting.model.Authorization
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@BlackBox
@Slf4j
class AuthorizationController {

    @Autowired
    AuthorizationGranting authorizationGranting

    @PostMapping(value = "/ascend/authorization")
    @ResponseBody
    @CompileDynamic
    @BlackBox(level = CarburetorLevel.METHOD)
    Authorization postAuthorization(@RequestBody Authorization iAuthorization) {
        return authorizationGranting.authorizationGranting(iAuthorization)
    }

}
