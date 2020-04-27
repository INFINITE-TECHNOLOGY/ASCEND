package io.infinite.ascend.web.controllers

import groovy.util.logging.Slf4j
import io.infinite.ascend.granting.configuration.entities.PrototypeAuthorization
import io.infinite.ascend.granting.configuration.repositories.PrototypeAuthorizationRepository
import io.infinite.blackbox.BlackBox
import io.infinite.carburetor.CarburetorLevel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@BlackBox(level = CarburetorLevel.METHOD)
@Slf4j
class InquireController {

    @Autowired
    PrototypeAuthorizationRepository prototypeAuthorizationRepository

    @GetMapping(value = "/ascend/public/granting/inquire")
    @ResponseBody
    Set<PrototypeAuthorization> inquire(
            @RequestParam("scopeName") String scopeName,
            @RequestParam("serverNamespace") String serverNamespace
    ) {
        Set<PrototypeAuthorization> persistentAuthorizations = prototypeAuthorizationRepository.inquire(scopeName, serverNamespace)
        return persistentAuthorizations
    }

}
