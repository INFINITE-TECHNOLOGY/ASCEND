package io.infinite.ascend

import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.hateoas.config.EnableHypermediaSupport

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
@Slf4j
@EnableZuulProxy
class ValidationApp implements CommandLineRunner {

    static void main(String[] args) {
        SpringApplication.run(ValidationApp.class, args)
    }

    @Override
    void run(String... args) throws Exception {
        runWithLogging()
    }

    @BlackBox
    void runWithLogging() {
        log.info("Starting Ascend Validation App...")
    }

}
