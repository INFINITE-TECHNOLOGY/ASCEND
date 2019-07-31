package io.infinite.ascend

import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import io.infinite.ascend.config.repositories.AscendInstance
import io.infinite.blackbox.BlackBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext
import org.springframework.hateoas.config.EnableHypermediaSupport

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
@Slf4j
class App implements CommandLineRunner {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    AscendInstance ascendInstanceRepository

    @Value('${ascendConfigInitPluginDir}')
    String ascendConfigInitPluginDir

    static void main(String[] args) {
        SpringApplication.run(App.class, args)
    }

    @Override
    void run(String... args) throws Exception {
        runWithLogging()
    }

    @BlackBox
    void runWithLogging() {
        log.info("Starting Ascend...")
        io.infinite.ascend.config.entities.AscendInstance ascendInstance = ascendInstanceRepository.getAscendInfo()
        if (ascendInstance == null) {
            log.info("Loading configuration data")
            Binding binding = new Binding()
            binding.setVariable("ascendInstanceRepository", ascendInstanceRepository)
            getAuthenticationGroovyScriptEngine().run("ConfigInit.groovy", binding)
            ascendInstance = new io.infinite.ascend.config.entities.AscendInstance()
            ascendInstance.isDataInitialized = true
            ascendInstanceRepository.saveAndFlush(ascendInstance)
        }
    }

    @Memoized
    GroovyScriptEngine getAuthenticationGroovyScriptEngine() {
        return new GroovyScriptEngine(ascendConfigInitPluginDir, this.getClass().getClassLoader())
    }

}
