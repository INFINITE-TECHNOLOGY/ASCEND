package io.infinite.ascend.granting.controllers

import groovy.util.logging.Slf4j
import io.infinite.ascend.config.entities.AuthorizationType
import io.infinite.ascend.config.repositories.AuthorizationTypeRepository
import io.infinite.blackbox.BlackBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@BlackBox
@Slf4j
class AuthorizationTypesController {

    @Autowired
    AuthorizationTypeRepository authorizationTypeRepository

    @GetMapping(value = "/ascend/authorizationTypes/search/findByScopeName")
    @ResponseBody
    Set<AuthorizationType> getAuthorizationTypes(
            @RequestParam("scopeName") String iScopeName
    ) {
        Set<AuthorizationType> authorizationTypes = authorizationTypeRepository.findByScopeName(iScopeName)
        return authorizationTypes
    }

}
