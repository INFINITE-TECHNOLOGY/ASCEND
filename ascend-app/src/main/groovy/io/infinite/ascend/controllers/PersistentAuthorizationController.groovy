package io.infinite.ascend.controllers


import groovy.util.logging.Slf4j
import io.infinite.ascend.config.entities.PersistentAuthorization
import io.infinite.ascend.config.repositories.PersistentAuthorizationRepository
import io.infinite.blackbox.BlackBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@BlackBox
@Slf4j
class PersistentAuthorizationController {

    @Autowired
    PersistentAuthorizationRepository persistentAuthorizationRepository

    @GetMapping(value = "/ascend/persistentAuthorizations/search/findByScopeName")
    @ResponseBody
    Set<PersistentAuthorization> getAuthorizationTypes(
            @RequestParam("scopeName") String iScopeName
    ) {
        Set<PersistentAuthorization> persistentAuthorizations = persistentAuthorizationRepository.findByScopeName(iScopeName)
        return persistentAuthorizations
    }

}
