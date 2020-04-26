package io.infinite.ascend

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.hateoas.config.EnableHypermediaSupport

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
@Slf4j
class AscendSecaas implements CommandLineRunner {

    @Autowired
    ConfigInitService configInitService

    static void main(String[] args) {
        System.setProperty("ascendValidationUrl", "")
        System.setProperty("ascendClientPublicKeyName", "")
        System.setProperty("ascendClientPrivateKey", "")
        SpringApplication.run(AscendSecaas.class, args)
    }

    @Override
    void run(String... args) throws Exception {
        runWithLogging()
    }

    @BlackBox
    void runWithLogging() {
        log.info("Starting Ascend...")
    }

}
